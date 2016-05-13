package org.kirhgoff.lastobot


import java.util.Objects

import akka.actor.FSM
import info.mukel.telegram.bots.api.Message
import org.kirhgoff.lastobot.Phrase._

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserTextMessage(msg:Message) extends UserMessages

//received commands
object Command {
  final case class Start()
  final case class ChangeLocale()
  final case class Obey()
  final case class Eat()
  final case class Abuse()
  final case class Smoke(count:Int)
  final case class SmokingStats()
}

//Feedback messages
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

//states
sealed trait State
case object Serving extends State
case object Abusing extends State
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
class SmokeBot(val senderId: Int, val userStorage: UserStorage) extends FSM[State, Data] {

  //By default bot is english
  //TODO make it implicit
  implicit var locale:BotLocale = userStorage.getLocaleOr(English)

  startWith(Serving, Empty)

  when(Serving) {
    case Event(Command.Obey, _) => {
      sender() ! Text(senderId, obey)
      stay
    }
    case Event(Command.Eat, _) => {
      sender() ! Keyboard(senderId, whatFoodToServe,
        Array(foodChoices))
      stay
    }
    case Event(Command.Abuse, _) =>
      sender() ! Keyboard(senderId,
        sayYes,
        Array(Phrase.yesNo))
      goto(Abusing)
    case Event(Command.Smoke(count), _) => goto(ConfirmingSmoke) using UserSmoked(count)
    case Event(Command.SmokingStats, _) => goto(ShowingStats)
    case Event(UserSaid(text), _) => goto(Serving)
    case Event(Command.Start, _) => {
      sender() ! Text(senderId, intro)
      stay
    }
    case Event(Command.ChangeLocale, _) => goto(ChangingLocale)
  }

  when(ShowingStats) {
    case _ => goto(Serving)
  }

  when(Abusing) {
    case Event(UserSaid(text), Empty) => goto(Serving) using UserSaid(text)
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
    case Abusing -> Serving => nextStateData match {
      case UserSaid(text) if Recognizer.yes(text) =>
        sender() ! Text(senderId, abuseReply(locale))
      case _ =>
    }
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
    case Serving -> ShowingStats => {
      val smoked = userStorage.smokedOverall()
      sender() ! Text(senderId, smokedOverall(smoked))
    }
    // Locale Changing
    case Serving -> ChangingLocale => nextStateData match {
      case UserChangedLocale(count) =>
        sender() ! Keyboard(senderId, changeLocale, Array(englishRussian))
      case other => println("wrong state Serving -> ChangingLocale: " + other)
    }
    case ChangingLocale -> Serving => nextStateData match {
      case UserChangedLocale(newLocale) =>
        locale = userStorage.updateLocale(newLocale)
      case other => println("wrong state ChangingLocale -> Serving: " + other)
    }
  }

  initialize()
}
