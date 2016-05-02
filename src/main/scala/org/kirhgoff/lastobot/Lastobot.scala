package org.kirhgoff.lastobot

import akka.actor.FSM
import info.mukel.telegram.bots.api.Message

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserTextMessage(msg:Message) extends UserMessages

//received commands
object Command {
  final case class Obey()
  final case class Eat()
  final case class Abuse()
  final case class Smoke(count:Int)
  final case class SmokingStats()
}

//Feedback messages
final case class Text(sender:Int, text:String)
final case class Keyboard(sender:Int, text:String, buttons:Array[Array[String]])

//states
sealed trait State
case object Serving extends State
case object ChoosingFood extends State
case object Abusing extends State
case object ReportingSmoke extends State
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
class Lastobot(val senderId: Int, val userStorage: UserStorage) extends FSM[State, Data] {

  //TODO add language setting

  startWith(Serving, Empty)

  when(Serving) {
    case Event(Command.Obey, _) => {
      sender() ! Text(senderId, "Yes, my master!")
      goto(Serving)
    }
    case Event(Command.Eat, _) => goto(ChoosingFood)
    case Event(Command.Abuse, _) => goto(Abusing)
    case Event(Command.Smoke(count), _) => goto(ConfirmingSmoke) using UserSmoked(count)
    case Event(Command.SmokingStats, _) => {
      println("Going to ShowingStats")
      goto(ShowingStats)
    }
    case Event(UserSaid(text), _) => goto(Serving)
  }

  when(ShowingStats) {
    case _ => goto(Serving)
  }

  when(ChoosingFood) {
    case _ => goto(Serving)
  }

  when(Abusing) {
    case Event(UserSaid(text), Empty) => goto(Serving) using UserSaid(text)
    case _ => goto(Serving)
  }

  when(ConfirmingSmoke) {
    case Event(UserSaid(text), _) if text.startsWith("да") =>
      goto(Serving) using Yes
    case Event(UserSaid(text), _) if text.startsWith("нет") =>
      goto(Serving) using No
    case _ =>
      goto(Serving) using What

  }

  onTransition {
    case Serving -> ChoosingFood =>
      sender() ! Keyboard(senderId,
        "What food may I serve you, my master?",
        Array(Array("bread", "butter", "beer")))
    case Serving -> Abusing =>
      sender() ! Keyboard(senderId,
        "Скажи да",
        Array(Array("да", "нет")))
    case Abusing -> Serving => nextStateData match {
      case UserSaid(text) if text.startsWith("да") =>
        sender() ! Text(senderId, "Манда!")
    }
    case Serving -> ConfirmingSmoke => nextStateData match {
      case UserSmoked(count) => sender() ! Keyboard(senderId,
        s"Вы выкурили $count сигарет(у)?",
        Array(Array("да", "нет")))
      case _ =>
    }
    case ConfirmingSmoke -> Serving => nextStateData match {
      case Yes => stateData match {
        case UserSmoked(count) => {
          userStorage.smoked(count)
          sender() ! Text(senderId, s"Done, you smoked $count cigarettes, master")
        }
        case _ => sender() ! Text(senderId, s"You got me confused")
      }
      case _ => sender() ! Text(senderId, "OK, cancelled.")
    }
    case Serving -> ShowingStats => {
      println("ShowingStats -> Serving")
      val smoked = userStorage.smokedOverall()
      sender() ! Text(senderId, s"Master, you smoke $smoked cigarettes overall")
    }
  }

  initialize()
}
