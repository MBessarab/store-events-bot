package standup.store.utils

import java.time.Duration

object Helpers {
  implicit class ScalaDurationExtended(d: scala.concurrent.duration.Duration) {
    def asJavaDuration: Duration =
      java.time.Duration.ofNanos(d.toNanos)
  }
}
