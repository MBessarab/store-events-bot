package standup.store.services.client.http

import cats.effect.Resource
import org.http4s.Response
import org.http4s.client.Client
import standup.store.errors.HttpError
import zio.{Has, IO, Task, URLayer, ZLayer}

object HttpClient {
  type HttpClient = Has[Service]

  trait Service {
    def run(uri: String): IO[HttpError, Resource[Task, Response[Task]]]
  }

  def http4s: URLayer[Has[Client[Task]], Has[Service]] =
    ZLayer.fromService[Client[Task], Service] { http4sClient: Client[Task] =>
      Http4s(http4sClient)
    }
}
