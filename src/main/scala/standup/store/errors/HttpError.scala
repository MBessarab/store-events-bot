package standup.store.errors

sealed trait HttpError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object HttpError {
  case class RequestError(msg: String) extends HttpError {
    override def message: String = msg
  }

  final case class MalformedUrl(url: String) extends HttpError {
    def message: String = s"Couldn't build url: $url"
  }
}