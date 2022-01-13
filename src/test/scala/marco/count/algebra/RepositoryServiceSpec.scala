package marco.count.algebra

import cats.effect.IO
import marco.count.model.domain.CacheConfig
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import cats.effect._

class RepositoryServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A RepositoryServiceSpec" - {
    "update a value if does not exist" in {
      for {
        implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
        repo <- RepositoryService.makeCache[IO](CacheConfig(10 minutes, 100))
        service = RepositoryService.make(repo)
        _ <- service.update("newKey", 3)
        amount <- service.get("newKey")
      } yield amount should matchPattern {
        case Some(3) =>
      }
    }

    "update a value if exist" in {
      for {
        implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
        repo <- RepositoryService.makeCache[IO](CacheConfig(10 minutes, 100))
        service = RepositoryService.make(repo)
        _ <- service.update("newKey", 3)
        _ <- service.update("newKey", 3)
        amount <- service.get("newKey")
      } yield amount should matchPattern {
        case Some(6) =>
      }
    }

    "get a non existing key" in {
      for {
        implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
        repo <- RepositoryService.makeCache[IO](CacheConfig(10 minutes, 100))
        service = RepositoryService.make(repo)
        amount <- service.get("newKey")
      } yield amount should matchPattern {
        case None =>
      }
    }
  }

}
