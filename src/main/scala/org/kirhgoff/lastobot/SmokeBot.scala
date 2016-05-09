package org.kirhgoff.lastobot


import java.util.Objects
import java.util.Arrays

import akka.actor.FSM
import info.mukel.telegram.bots.api.Message
import org.apache.commons.lang3.ArrayUtils

import org.kirhgoff.lastobot.Phrase._

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserTextMessage(msg:Message) extends UserMessages

//received commands
object Command {
  final case class Start()
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

//data
sealed trait Data
case object Empty extends Data
case object Yes extends Data
case object No extends Data
case object What extends Data
final case class UserSaid(text:String) extends Data
final case class UserSmoked(count:Int) extends Data

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
      sender() ! Text(senderId, obey(locale))
      stay
    }
    case Event(Command.Eat, _) => {
      sender() ! Keyboard(senderId, whatFoodToServe(locale),
        Array(foodChoices(locale)))
      stay
    }
    case Event(Command.Abuse, _) =>
      sender() ! Keyboard(senderId,
        sayYes(locale),
        Array(Phrase.yesNo(locale)))
      goto(Abusing)
    case Event(Command.Smoke(count), _) => goto(ConfirmingSmoke) using UserSmoked(count)
    case Event(Command.SmokingStats, _) => goto(ShowingStats)
    case Event(UserSaid(text), _) => goto(Serving)
    case Event(Command.Start, _) => {
      sender() ! Text(senderId, intro(locale))
      stay
    }
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
        case _ => sender() ! Text(senderId, what(locale))
      }
      case _ => sender() ! Text(senderId, cancelled(locale))
    }
    case Serving -> ShowingStats => {
      val smoked = userStorage.smokedOverall()
      sender() ! Text(senderId, smokedOverall(smoked))
    }
  }

  initialize()
}
