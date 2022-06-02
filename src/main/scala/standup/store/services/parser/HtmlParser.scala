package standup.store.services.parser

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.model.ElementNode
import org.http4s.Response
import standup.store.models.Event
import standup.store.services.parser.HtmlParser.HtmlTags.Attributes._
import standup.store.services.parser.HtmlParser.HtmlTags.Classes._
import standup.store.services.parser.HtmlParser.HtmlTags._
import zio.interop.catz.taskConcurrentInstance
import zio.{Task, ZIO}

import scala.util.Try

case class HtmlParser() extends StandUpStoreResource.Service {
  override def parse(response: Response[Task]): Task[Seq[Event]] = {
    for {
      htmlStr <- response.as[String]
      html <- ZIO.succeed(new JsoupBrowser().parseString(htmlStr))
    } yield getEvents(html)
  }

  def getEvents(html: JsoupDocument): Seq[Event] = {
    html.body.select(Ticket)
      .flatMap(_.childNodes)
      .collect {
        case _ @ ElementNode(el) if MainElementsContainer.contains(el.tagName) =>
          val img = el.select(Img)
            .headOption
            .map(_.attr(Src))
            .getOrElse("")

          val status = el.select(TicketStatus)
            .headOption
            .map(_.text)
            .getOrElse("")

          val date = el.select(TicketDate)
            .headOption
            .map(_.text.trim)
            .getOrElse("")

          val time = el.select(TicketTime)
            .headOption
            .map(_.text)
            .getOrElse("")

          val price = el.select(TicketPrice)
            .headOption
            .map(_.text)
            .getOrElse("")

          val urlO = Try(el.attr(Href))
            .toOption

          Event(
            date = date,
            time = time,
            price = price,
            status = status,
            urlO = urlO,
            image = img
          )
      }
      .toSeq
  }

}

object HtmlParser {
  object HtmlTags {
    val Img = "img"
    val A = "a"
    val Div = "div"
    val MainElementsContainer = Seq(A, Div)

    object Attributes {
      val Src = "src"
      val Href = "href"
    }

    object Classes {
      val Ticket = ".ticket"
      val TicketStatus = ".ticket__status"
      val TicketDate = ".ticket__date"
      val TicketTime = ".ticket__time"
      val TicketPrice = ".ticket__price"
    }
  }
}