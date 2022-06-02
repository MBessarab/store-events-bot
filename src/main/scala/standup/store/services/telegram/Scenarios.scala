package standup.store.services.telegram

import canoe.api._
import canoe.syntax._
import standup.store.services.db.chat.DatabaseClient
import zio.Task

case class Scenarios(
    telegramClient: TelegramClient[Task],
    database: DatabaseClient.Service
) extends CanoeScenarios.Service {
  private implicit val canoe: TelegramClient[Task] = telegramClient

  override def start: Scenario[Task, Unit] = {
    for {
      chat <- Scenario.expect(command("start").chat)
      _ <- Scenario.eval(chat.send("Subscribed"))
        .handleErrorWith { err => Scenario.done }
      _ <- Scenario.eval(database.add(chat))
    } yield ()
  }


}
