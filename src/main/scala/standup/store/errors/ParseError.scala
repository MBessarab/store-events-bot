package standup.store.errors

sealed trait ParseError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object ParseError {
  case class ParsingError(msg: String) extends ParseError {
    override def message: String = msg
  }
}

