package standup.store.services.db.chat

import canoe.models.Chat
import zio.{Has, Task, ULayer, ZLayer}

object DatabaseClient {
  type DatabaseClient = Has[Service]

  trait Service {
    def all: Task[Set[Chat]]
    def add(chat: Chat): Task[Chat]
  }

  def run: ULayer[Has[Service]] = {
    ZLayer.succeed[Service](DBChatLayer())
  }
}
