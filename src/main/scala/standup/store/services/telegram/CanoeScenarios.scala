package standup.store.services.telegram

import canoe.api.{Scenario, TelegramClient}
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.db.chat.DatabaseClient.DatabaseClient
import zio.{Has, Task, URLayer, ZLayer}

object CanoeScenarios {
  type CanoeScenarios = Has[Service]

  trait Service {
    def start: Scenario[Task, Unit]
  }
  type ScenariosDeps = Has[TelegramClient[Task]] with DatabaseClient
  def run: URLayer[ScenariosDeps, Has[Service]] =
    ZLayer.fromServices[TelegramClient[Task], DatabaseClient.Service, Service] { (client, database) =>
      Scenarios(client, database)
    }
}
