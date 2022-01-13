package marco.count.algebra

import cats.effect.kernel.{Resource, Sync}

trait ProcessCmd[F[_]] {
  def asResource(path: String): Resource[F, Process]
}

object ProcessCmd {
  def make[F[_] : Sync]: ProcessCmd[F] = new ProcessCmd[F] {
    override def asResource(path: String): Resource[F, Process] =
      Resource.make[F, Process](Sync[F].delay {
        new ProcessBuilder(path).start()
      })(process => Sync[F].delay(process.destroy()))
  }
}