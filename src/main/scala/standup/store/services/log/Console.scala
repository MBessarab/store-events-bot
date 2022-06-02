package standup.store.services.log
import zio.{UIO, URIO}
import zio.clock.Clock
import zio.console.{Console => ConsoleZIO}

case class Console(
    clock: Clock.Service,
    console: ConsoleZIO.Service
) extends Logger.Service {
  override def trace(message: => String): UIO[Unit] = print(message)

  override def debug(message: => String): UIO[Unit] = print(message)

  override def info(message: => String): UIO[Unit] = print(message)

  override def warn(message: => String): UIO[Unit] = print(message)

  override def error(message: => String): UIO[Unit] = print(message)

  override def error(t: Throwable)(message: => String): UIO[Unit] = {
    for {
      _ <- print(message)
      _ <- console.putStrLn(s"[${t.getMessage}] [${t.getStackTrace.mkString("\n")}]").orDie
    } yield ()
  }

  private def print(message: String): URIO[Any, Unit] = {
    (for {
      timestamp <- clock.currentDateTime
      _ <- console.putStrLn(s"[$timestamp] [$message]")
    } yield ()).orDie
  }
}
