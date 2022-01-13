package marco.count.program

import cats.effect.kernel.Temporal
import cats.implicits._
import marco.count.algebra.RepositoryService
import marco.count.model.domain.{Event, WordCountConfig}
import org.typelevel.log4cats.Logger

import scala.language.postfixOps

trait WordCountProgram[F[_]] {
  def countWords(stream: fs2.Stream[F, Event], repo: RepositoryService[F]): fs2.Stream[F, Unit]
}

object WordCountProgram {
  def make[F[_] : Temporal : Logger](conf:WordCountConfig): WordCountProgram[F] = new WordCountProgram[F] {
    override def countWords(stream: fs2.Stream[F, Event], repo: RepositoryService[F]): fs2.Stream[F, Unit] = {
      stream
        .groupWithin(conf.maxChunkSize, conf.duration)
        .evalMap(chunkOfEvents =>
          Logger[F].info(s"Evaluate chunk size ${chunkOfEvents.size}") *>
          chunkOfEvents
            .toList
            .groupBy(_.eventType)
            .map { case (eventType, events) =>
              eventType -> events.map(_.data.split(" ").length).sum
            }.pure[F]
        )
        .evalTap(x => x.toList.traverse { case (key, value) => repo.update(key, value) })
        .void
    }
  }
}
