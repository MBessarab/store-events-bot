package standup.store.services.client.telegram

import canoe.api.models.ChatApi
import canoe.api.{Bot, TelegramClient => CanoeClient}
import canoe.models.ParseMode.ParseMode
import canoe.models.{Chat, InputFile, ParseMode, PrivateChat}
import canoe.models.messages.TextMessage
import canoe.models.outgoing.{PhotoContent, TextContent}
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.db.chat.DatabaseClient.DatabaseClient
import standup.store.services.telegram.CanoeScenarios
import zio.{Task, ZIO}
import zio.interop.catz._
import zio.interop.catz.implicits._

case class Client(
    client: CanoeClient[Task],
    scenario: CanoeScenarios.Service,
    database: DatabaseClient.Service
) extends TelegramClient.Service {

  private implicit val canoe: CanoeClient[Task] = client

  override def broadcastPhotoPost(message: String, urlPhoto: String, receivers: Set[Chat]): Task[Unit] = {
    ZIO.foreach(receivers) { chat =>
      val api = new ChatApi(PrivateChat(chat.id, None, None, None))
      api.send(PhotoContent(photo = InputFile.fromUrl(urlPhoto), caption = message, parseMode = Some(ParseMode.MarkdownV2)))
        .catchAll(err => Task.unit)
    }.unit
  }

  override def init: Task[Unit] = {
    Bot
      .polling[Task]
      .follow(
        scenario.start
      )
      .compile
      .drain
  }
}
