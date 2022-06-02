package standup.store.services.parser

import org.http4s.Response
import standup.store.models.Event
import zio.{Has, Task, ULayer, ZLayer}

object StandUpStoreResource {

  type StandUpStoreResource = Has[Service]

  trait Service {
    def parse(response: Response[Task]): Task[Seq[Event]]
  }

  def html: ULayer[Has[Service]] =
    ZLayer.succeed[Service](HtmlParser())

}
