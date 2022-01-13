package marco.count.program

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.concurrent.Topic
import marco.count.algebra.RepositoryService
import marco.count.model.domain.{Event, WordCountConfig}

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.scalamock.scalatest.AsyncMockFactory

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class WordCountProgramSpec  extends AsyncFreeSpec with AsyncIOSpec with Matchers with AsyncMockFactory {
"A Word Count Program" - {
  "if time window is ended then repo is updated"  in {
    val repo = mock[RepositoryService[IO]]
    for {
      implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
      service = WordCountProgram.make[IO](WordCountConfig(1 seconds,100, 100))
      stream = fs2.Stream.emits[IO,Event](Seq(Event("ev3","string", 12312L)))
      _ = ( repo.update _).expects(*,*).returning(IO.pure(())).once()
      x <- service.countWords(stream,repo).compile.drain.start
      _ <- x.join
    } yield ()
  }

  "if time window is not ended but max amount is reach then repo is updated"  in {
    val repo = mock[RepositoryService[IO]]
    for {
      implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
      service = WordCountProgram.make[IO](WordCountConfig(1 hours,100, 1))
      stream = fs2.Stream.emits[IO,Event](Seq(Event("ev4","string", 12312L)))
      _ = ( repo.update _).expects(*,*).returning(IO.pure(())).once()
      x <- service.countWords(stream,repo).compile.drain.start
      _ <- x.join
    } yield ()
  }
}
}
