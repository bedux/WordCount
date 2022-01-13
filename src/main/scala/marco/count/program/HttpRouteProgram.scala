package marco.count.program

import cats.effect.kernel.Async
import cats.syntax.semigroupk._
import marco.count.model.domain.EndpointConfig
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait HttpRouteProgram[F[_]] {
  def httpRoute(routeDefinition: HttpRoutes[F],routeDefinitions: HttpRoutes[F]*):F[Unit]
}


object HttpRouteProgram {
  def make[F[_] : Async](endpointConfig: EndpointConfig): HttpRouteProgram[F] =
      new HttpRouteProgram[F] {
        override def httpRoute(routeDefinition: HttpRoutes[F], routeDefinitions: HttpRoutes[F]*): F[Unit] = {
          BlazeServerBuilder[F]
            .withExecutionContext(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4)))
            .bindHttp(endpointConfig.port, endpointConfig.host)
            .withHttpApp(Router[F]("/" -> (routeDefinitions.fold(routeDefinition)(_ <+> _))).orNotFound)
            .serve
            .compile
            .drain
        }
    }
}