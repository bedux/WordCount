package marco.count.program

import cats.Applicative
import fs2.concurrent.Topic
import fs2.text
import io.circe.Decoder
import io.circe.parser.parse
import marco.count.model.domain.Event
import org.typelevel.log4cats.Logger
import cats.implicits._

trait IngestProgram[F[_]] {
  def ingest(f: fs2.Stream[F, Byte], successTopic: Topic[F, Event], errorTopic: Topic[F, Throwable]): fs2.Stream[F, Either[Throwable, Event]]
}

object IngestProgram {
  def make[F[_] : Applicative: Logger] =
    new IngestProgram[F] {
    implicit val decoder = Decoder.instance { hC =>
      for {
        eventType <- hC.downField("event_type").as[String]
        data <- hC.downField("data").as[String]
        timestamp <- hC.downField("timestamp").as[Long]
      } yield Event(eventType, data, timestamp)
    }

    def parseRawMessage(rawMessage: String): Either[Throwable, Event] =
      parse(rawMessage).flatMap(x =>
        x.as[Event] match {
          case Left(value) =>
            Left(new Throwable(value.message))
          case Right(value) =>
            Right(value)
        }
      )


    override def ingest(rawEventStream: fs2.Stream[F, Byte], successTopic: Topic[F, Event], errorTopic: Topic[F, Throwable]): fs2.Stream[F, Either[Throwable, Event]] =
      rawEventStream
        .through(text.utf8.decode)
        .through(text.lines)
        .map(parseRawMessage)
        .evalTap {
          case Right(value) =>
            Logger[F].info(s"Receive event_type: ${value.eventType}") *>
              successTopic.publish1(value)
          case Left(error) =>
            Logger[F].info(s"Invalid message: ${error.getMessage}") *>
              errorTopic.publish1(error)
        }
  }
}