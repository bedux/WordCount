package marco.count.program

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.concurrent.Topic
import marco.count.model.domain.Event
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class IngestProgramSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with AsyncMockFactory{

  "An Ingest Program" - {
    "When invalid item is publish then error is publish in error stream" in {
      for {
        implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
        events <- Topic[IO,Event]
        error <- Topic[IO,Throwable]
        stream = fs2.Stream[IO,Byte]("wrong event \n".getBytes:_*)
        x <- error.subscribe(2).take(1).interruptAfter(2 seconds).compile.drain.start
        _ <- IngestProgram.make[IO].ingest(stream,events,error).take(1).compile.drain
        _ <- x.join
      } yield ()
    }

    "When valid item is publish then event is publish in event stream" in {
      for {
        implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
        events <- Topic[IO,Event]
        error <- Topic[IO,Throwable]
        stream = fs2.Stream[IO,Byte]("""{ "event_type": "test", "data": "asd", "timestamp": 123 } """.getBytes:_*)
        x <- IngestProgram.make[IO].ingest(stream,events,error).take(1)
          .concurrently(events.subscribe(2).take(1).interruptAfter(2 seconds)).compile.toList
      } yield x.length shouldBe 1
    }
  }
}
