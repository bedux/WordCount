package marco.count.model

import scala.concurrent.duration.FiniteDuration

object domain {
  final case class Event(eventType: String, data: String, timestamp: Long)

  final case class WordCountConfig(duration: FiniteDuration, maxSize: Int, maxChunkSize: Int)

  final case class EndpointConfig(port: Int, host: String)

  final case class CacheConfig(expireAfter: FiniteDuration, maxSize: Int)
}
