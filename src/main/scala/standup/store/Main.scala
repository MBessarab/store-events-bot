package standup.store

import canoe.api.{TelegramClient => CanoeClient}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import standup.store.config.Config
import standup.store.services.client.http.HttpClient
import standup.store.services.client.telegram.TelegramClient
import standup.store.services.client.telegram.TelegramClient.TelegramClient
import standup.store.services.db.chat.DatabaseClient
import standup.store.services.event.EventChecker
import standup.store.services.event.EventChecker.EventChecker
import standup.store.services.log.Logger
import standup.store.services.parser.StandUpStoreResource
import standup.store.services.telegram.CanoeScenarios
import zio._
import zio.clock.Clock
import zio.console.putStrLn
import standup.store.utils.Helpers.ScalaDurationExtended
import zio.interop.catz._

object Main extends zio.App {
  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    (for {
      http4sClient <- makeHttpClient
      config <- getConfig.orDie
      canoeClient <- makeCanoeClient(config)
      _ <- makeProgram(http4sClient, canoeClient, config)
    } yield ())
      .tapError(err => putStrLn(s"Execution failed with: ${err.getMessage}"))
      .exitCode
  }

  private val getConfig: IO[Throwable, Config] =
    ZIO
      .fromEither(ConfigSource.default.load[Config])
      .mapError(failures => new RuntimeException(failures.prettyPrint()))

  private def makeHttpClient: UIO[TaskManaged[Client[Task]]] =
    ZIO
      .runtime[Any]
      .map { implicit rts =>
        BlazeClientBuilder
          .apply[Task](platform.executor.asEC)
          .resource
          .toManaged
      }

  private def makeCanoeClient(config: Config): UIO[TaskManaged[CanoeClient[Task]]] = {
    for {
      client <- ZIO
        .runtime[Any]
        .map { implicit rts =>
          CanoeClient
            .global[Task](config.telegram.credentials.token)
            .toManaged
        }
    } yield client
  }

  private def makeProgram(
      http4sClient: TaskManaged[Client[Task]],
      canoeClient: TaskManaged[CanoeClient[Task]],
      config: Config
  ): ZIO[ZEnv, Throwable, Long] = {
    val loggerLayer = Logger.console

    val databaseLayer = DatabaseClient.run

    val configLayer = getConfig.toLayer.orDie

    val http4sClientLayer = http4sClient.toLayer.orDie
    val httpClientLayer = http4sClientLayer >>> HttpClient.http4s

    val parserResource = StandUpStoreResource.html

    val canoeClientLayer = canoeClient.toLayer.orDie
    val telegramScenarioLayer = (canoeClientLayer ++ databaseLayer) >>> CanoeScenarios.run
    val telegramClientLayer = (canoeClientLayer ++ telegramScenarioLayer ++ databaseLayer) >>> TelegramClient.run

    val eventCheckerLayer = (loggerLayer ++ httpClientLayer ++ parserResource ++ telegramClientLayer ++ configLayer ++ databaseLayer) >>> EventChecker.live

    val eventCheckerScheduleLayer =
      ZIO
        .accessM[EventChecker with Clock](_.get.scheduleRefresh)
        .repeat(Schedule.spaced(config.standUpStore.delay.asJavaDuration))

    val startTelegramClient =
      ZIO.accessM[TelegramClient](_.get.init)

    val programLayer = eventCheckerLayer ++ telegramClientLayer
    val program = startTelegramClient.fork *> eventCheckerScheduleLayer

    program.provideSomeLayer[ZEnv](programLayer)
  }
}
