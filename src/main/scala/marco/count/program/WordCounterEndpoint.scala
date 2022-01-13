package marco.count.program

import cats.effect.Async
import marco.count.algebra.RepositoryService
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir._
import cats.implicits._
import org.http4s.HttpRoutes

object WordCounterEndpoint {
  val countCharactersEndpoint: Endpoint[Unit, String, String, Int, Any] =
    endpoint.in(path[String]("key")).get
      .out(plainBody[Int])
      .errorOut(plainBody[String])


  def make[F[_] : Async](repositoryService: RepositoryService[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      List(
        countCharactersEndpoint
          .serverLogic(key =>
            repositoryService
              .get(key)
              .map {
                case Some(count) => Right(count)
                case None => Left("No key found")
              }
          )))
}
