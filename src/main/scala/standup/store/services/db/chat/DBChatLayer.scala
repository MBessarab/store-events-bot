package standup.store.services.db.chat

import canoe.models.Chat
import zio.Task

case class DBChatLayer(

) extends DatabaseClient.Service {
  private var chats: Seq[Chat] = List[Chat]()

  override def all: Task[Set[Chat]] = Task.effect(chats.toSet)

  override def add(chat: Chat): Task[Chat] = Task.effect {
    chats = chats :+ chat
    chat
  }
}
