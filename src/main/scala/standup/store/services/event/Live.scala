package standup.store.services.event

import standup.store.config.Config
import standup.store.models.Event
import standup.store.services.client.http.HttpClient
import standup.store.services.client.telegram.TelegramClient
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.log.Logger
import standup.store.services.parser.StandUpStoreResource
import zio.interop.catz._
import zio.{Task, ZIO}

case class Live(
    logger: Logger.Service,
    httpClient: HttpClient.Service,
    parser: StandUpStoreResource.Service,
    config: Config,
    telegramClient: TelegramClient.Service,
    database: DatabaseClient.Service
) extends EventChecker.Service {

  private val url: String = config.standUpStore.url

  override def scheduleRefresh: Task[Unit] = {
    for {
      _ <- logger.info("Schedule task started")
      response <- httpClient.run(url)
      _ <- logger.info("Start parsing")
      events <- response.use(parser.parse)
      _ <- logger.info("Parse successfull")
      chats <- database.all
      _ <- ZIO.foreach(events) { event =>
        val message = makeMessage(event)
        val urlPhoto = s"https://$url/${event.image}"

        telegramClient.broadcastPhotoPost(message, urlPhoto, chats)
      }
    } yield ()
  }

  private def makeMessage(event: Event): String =
    s"""*${event.date} ${event.time}*
       |
       |${event.status}
       |""".stripMargin +
      event.urlO.map { s =>
        s"""
           |${event.price}
           |
           |[Билеты Здесь](https://$url$s)""".stripMargin
      }.getOrElse("")
}
