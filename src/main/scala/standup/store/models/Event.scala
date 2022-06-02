package standup.store.models

case class Event(
    date: String,
    time: String,
    price: String,
    status: String,
    urlO: Option[String],
    image: String
)
