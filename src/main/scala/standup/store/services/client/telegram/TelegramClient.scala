package standup.store.services.client.telegram

import canoe.api.{TelegramClient => CanoeClient}
import canoe.models.Chat
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.db.chat.DatabaseClient.DatabaseClient
import standup.store.services.telegram.CanoeScenarios
import standup.store.services.telegram.CanoeScenarios.CanoeScenarios
import zio.{Has, Task, URLayer, ZLayer}

object TelegramClient {
  type TelegramClient = Has[Service]

  trait Service {
    def broadcastPhotoPost(message: String, urlPhoto: String, receivers: Set[Chat]): Task[Unit]
    def init: Task[Unit]
  }

  type ClientDeps = Has[CanoeClient[Task]] with CanoeScenarios with DatabaseClient
  def run: URLayer[ClientDeps, TelegramClient] =
    ZLayer.fromServices[CanoeClient[Task], CanoeScenarios.Service, DatabaseClient.Service, Service] {
      (client, scenarios, database) =>
        Client(client, scenarios, database)
    }
}
