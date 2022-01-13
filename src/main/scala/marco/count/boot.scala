package marco.count

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import fs2.concurrent.Topic
import marco.count.algebra._
import marco.count.program._
import marco.count.model.domain._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._

import scala.language.postfixOps

object boot extends IOApp {
  def run(arg: List[String]): IO[ExitCode] =
    for {
      implicit0(logger:SelfAwareStructuredLogger[IO]) <- Slf4jLogger.create[IO]
      successTp <- Topic[IO, Event]
      failureTp <- Topic[IO, Throwable]

      _ <- logger.info("--- Read configs ---")
      config <- IO(ConfigFactory.load.getConfig("word-count"))
      source = ConfigSource.fromConfig(config)

      blackBoxPath <- IO(source.at("blackbox").at("path").loadOrThrow[String])
      streamConf <- IO(source.at("word-count-stream").loadOrThrow[WordCountConfig])
      endpointConfig <- IO(source.at("endpoint").loadOrThrow[EndpointConfig])
      cacheConfig <- IO(source.at("cache").loadOrThrow[CacheConfig])

      _ <- logger.info("--- Create Algebras ---")
      repoCache <- RepositoryService.makeCache[IO](cacheConfig)
      repoService = RepositoryService.make(repoCache)

      _ <- logger.info("--- Create Programs ---")
      wordCountProgram = WordCountProgram.make[IO](streamConf)
      ingestProgram = IngestProgram.make[IO]
      httpProgram = HttpRouteProgram.make[IO](endpointConfig)

      _ <- logger.info("--- Run programs ---")
      _ <- ProcessCmd.make[IO].asResource(blackBoxPath).use { process =>
        for {
          wcProgramFiber <- wordCountProgram.countWords(successTp.subscribe(streamConf.maxSize), repoService).compile.drain.start
          ingestionProgramFiber <- ingestProgram.ingest(fs2.io.readInputStream( IO.delay(process.getInputStream), 100), successTp, failureTp).compile.drain.start

          _ <- logger.info("--- Run HttpServer ---")
          countEndpoint = WordCounterEndpoint.make(repoService)
          _ <- httpProgram.httpRoute(countEndpoint)
          _ <- wcProgramFiber.join
          _ <- ingestionProgramFiber.join
        }yield ()
      }
    } yield ExitCode.Success
}
