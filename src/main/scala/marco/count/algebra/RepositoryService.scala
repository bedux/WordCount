package marco.count.algebra

import cats.{Applicative, Monad}
import cats.effect.Ref
import cats.effect.kernel.Ref.Make
import cats.implicits._
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import marco.count.model.domain.CacheConfig
import org.typelevel.log4cats.Logger


trait RepositoryService[F[_]] {
  def update(key: String, value: Int): F[Unit]

  def get(key: String): F[Option[Int]]
}

object RepositoryService {
  def makeCache[F[_] : Make : Applicative](cacheConfig:CacheConfig): F[Ref[F, Cache[String, Int]]] = Ref.of(Scaffeine()
    .recordStats()
    .expireAfterWrite(cacheConfig.expireAfter)
    .maximumSize(cacheConfig.maxSize)
    .build[String, Int]())

  def make[F[_] : Monad: Logger](cacheRef: Ref[F, Cache[String, Int]]): RepositoryService[F] =
    new RepositoryService[F] {
    override def update(key: String, value: Int): F[Unit] =
      cacheRef.modify { cache =>
        val oldValue = cache.get(key, _ => 0)
        cache.put(key, oldValue + value)
        (cache,oldValue + value)
      }.flatMap(finalValue => Logger[F].info(s"Updated $key -> $finalValue"))

      override def get(key: String): F[Option[Int]] =
      cacheRef.get.map(_.getIfPresent(key))
  }
}
