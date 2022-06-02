package standup.store.config

import standup.store.config.Config._

import scala.concurrent.duration.Duration

case class Config(
    telegram: TelegramCfg,
    standUpStore: StandUpStoreCfg
)

object Config {
  case class Credentials(token: String)
  case class TelegramCfg(credentials: Credentials)
  case class StandUpStoreCfg(url: String, delay: Duration)
}
