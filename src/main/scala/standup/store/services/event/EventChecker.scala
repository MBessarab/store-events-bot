package standup.store.services.event

import standup.store.config.Config
import standup.store.models.Event
import standup.store.services.client.http.HttpClient
import standup.store.services.client.http.HttpClient.HttpClient
import standup.store.services.client.telegram.TelegramClient
import standup.store.services.client.telegram.TelegramClient.TelegramClient
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.db.chat.DatabaseClient.DatabaseClient
import standup.store.services.log.Logger
import standup.store.services.log.Logger.Logger
import standup.store.services.parser.StandUpStoreResource
import standup.store.services.parser.StandUpStoreResource.StandUpStoreResource
import zio.{Has, Task, ZLayer}

object EventChecker {
  type EventChecker = Has[Service]

  trait Service {
    def scheduleRefresh: Task[Unit]
  }

  type LiveDeps = Logger with HttpClient with StandUpStoreResource with TelegramClient with Has[Config] with DatabaseClient
  def live: ZLayer[LiveDeps, Nothing, Has[Service]] =
    ZLayer.fromServices[
      Logger.Service,
      HttpClient.Service,
      StandUpStoreResource.Service,
      TelegramClient.Service,
      Config,
      DatabaseClient.Service,
      Service
    ] {
      (logger, httpClient, resource, tgClient, config, database) =>
        Live(
          logger = logger,
          httpClient = httpClient,
          parser = resource,
          config = config,
          telegramClient = tgClient,
          database = database
        )
    }
}
