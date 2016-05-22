package org.kirhgoff.lastobot

import java.time.LocalDate
import java.util.Objects

import akka.actor.FSM
import com.typesafe.scalalogging.LazyLogging
import info.mukel.telegram.bots.api.Message
import org.kirhgoff.lastobot.Phrase._

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserTextMessage(msg:Message) extends UserMessages

//What commands bot processes
object Command {
  final case class Start()
  final case class ChangeLocale()
  final case class Smoke(count:Int)
  final case class SmokingStats()
}

//What it sends back
final case class Text(sender:Int, text:String)
final case class Keyboard(sender:Int, text:String, buttons:Array[Array[String]]) {
  override def equals(other: Any) = other match {
    case Keyboard (s, t, b) if s == sender &&
      ((t == null && text == null) || t.equals(text)) &&
      Objects.deepEquals(b, buttons) => true
    case _ => false
  }
  override def toString =
    s"Keyboard for $sender text=($text) buttons=${buttons.deep.mkString}"

}
final case class Picture(sender:Int, filePath:String)

//states
sealed trait State
case object Serving extends State
case object ConfirmingSmoke extends State
case object ShowingStats extends State
case object ChangingLocale extends State

//data
sealed trait Data
case object Empty extends Data
case object Yes extends Data
case object No extends Data
case object What extends Data
final case class UserSaid(text:String) extends Data
final case class UserSmoked(count:Int) extends Data
final case class UserChangedLocale(locale:BotLocale) extends Data


/**
  * Created by kirilllastovirya on 26/04/2016.
  */
class SmokeBot(val senderId: Int, val userStorage: UserStorage) extends FSM[State, Data] with LazyLogging {

  //By default bot is english
  implicit var locale:BotLocale = userStorage.getLocaleOr(English)

  startWith(Serving, Empty)

  when(Serving) {
    case Event(Command.Smoke(count), _) => goto(ConfirmingSmoke) using UserSmoked(count)
    case Event(Command.SmokingStats, _) => goto(ShowingStats)
    case Event(UserSaid(text), _) => goto(Serving)
    case Event(Command.Start, _) =>
      sender() ! Text(senderId, intro)
      stay
    case Event(Command.ChangeLocale, _) => goto(ChangingLocale)
  }

  when(ShowingStats) {
    case _ => goto(Serving)
  }

  when(ConfirmingSmoke) {
    case Event(UserSaid(text), _) if Recognizer.yes(text) =>
      goto(Serving) using Yes
    case Event(UserSaid(text), _) if Recognizer.no(text) =>
      goto(Serving) using No
    case _ =>
      goto(Serving) using What
  }

  when(ChangingLocale) {
    case Event(UserSaid(text), _) if Recognizer.english(text) =>
      goto(Serving) using UserChangedLocale(English)
    case Event(UserSaid(text), _) if Recognizer.russian(text) =>
      goto(Serving) using UserChangedLocale(Russian)
    case _ =>
      goto(Serving) using What
  }

  onTransition {
    //------------------------------ Smoking ------------------------------
    case Serving -> ConfirmingSmoke => nextStateData match {
      case UserSmoked(count) => sender() ! Keyboard(senderId,
        youSmokeQuestion(count),
        Array(yesNo(locale)))
      case _ =>
    }
    case ConfirmingSmoke -> Serving => nextStateData match {
      case Yes => stateData match {
        case UserSmoked(count) => {
          userStorage.smoked(count)
          sender() ! Text(senderId, youSmokeConfirmed(count))
        }
        case _ => sender() ! Text(senderId, what)
      }
      case _ => sender() ! Text(senderId, cancelled)
    }
    //------------------------------ Stats ------------------------------
    case Serving -> ShowingStats => {
      val monthlyFilePath:String = ChartsBuilder.monthlyFile(
        userStorage.aggregatedByDateBefore(
          LocalDate.now.minusDays(30)
        ))
      sender() ! Picture(senderId, monthlyFilePath)

      val weeklyFilePath:String = ChartsBuilder.weeklyFile(
        userStorage.aggregatedByDateBefore(
          LocalDate.now.minusDays(7)
      ))
      sender() ! Picture(senderId, weeklyFilePath)
    }
    //------------------------------ Locale ------------------------------
    case Serving -> ChangingLocale =>
        sender() ! Keyboard(senderId, changeLocale, Array(englishRussian))

    case ChangingLocale -> Serving => nextStateData match {
      case UserChangedLocale(newLocale) =>
        locale = userStorage.updateLocale(newLocale)
      case other => logger.error(s"wrong state ChangingLocale -> Serving: $other")
    }
  }

  initialize()
}
