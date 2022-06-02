package standup.store.services.client.http

import cats.effect.Resource
import org.http4s.Uri.{Authority, RegName, Scheme}
import org.http4s.client.Client
import org.http4s.{Request, Response, Uri}
import standup.store.errors.HttpError
import zio.CanFail.canFailAmbiguous1
import zio.{IO, Task, UIO, ZIO}

case class Http4s(client: Client[Task]) extends HttpClient.Service {
  override def run(uri: String): IO[HttpError, Resource[Task, Response[Task]]] = {
    def call(): UIO[Resource[Task, Response[Task]]] = {
      val request: Request[Task] = Request(
        uri = Uri(
          scheme = Some(Scheme.https),
          authority = Some(Authority(host = RegName(uri)))
        )
      )

      ZIO
        .runtime[Any]
        .map { implicit rt =>
          client.run(request)
        }
    }

    call().mapError { ex =>
      HttpError.RequestError(ex.toString)
    }
  }
}
