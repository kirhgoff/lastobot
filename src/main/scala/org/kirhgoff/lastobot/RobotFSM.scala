package org.kirhgoff.lastobot

import akka.actor.FSM
import info.mukel.telegram.bots.api.Message

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserText(msg:Message) extends UserMessages

//What FSM receives
trait BotMessages
case class BotHearsCommand (commandName:String, args:Seq[String]) extends BotMessages
case class BotHearsText (text:String) extends BotMessages

//received commands
final case class Obey()
final case class Eat()
final case class Abuse()
final case class Smoke()
final case class UnknownCommand()

//sent messages
final case class Text(sender:Int, text:String)
final case class Keyboard(sender:Int, text:String, buttons:Array[Array[String]])

//states
sealed trait State
case object Serving extends State
case object ChoosingFood extends State
case object Abusing extends State

//data
sealed trait Data
case object Uninitialized extends Data
final case class UserSaid(text:String) extends Data

/**
  * Created by kirilllastovirya on 26/04/2016.
  */
class RobotFSM(val senderId: Int) extends FSM[State, Data] {
  import info.mukel.telegram.bots.OptionPimps._

  startWith(Serving, Uninitialized)

  when(Serving) {
    case Event(Obey, Uninitialized) => {
      //TODO redo with another state
      sender() ! Text(senderId, "Yes, my master!")
      goto(Serving)
    }
    case Event(Eat, Uninitialized) => {
      println("Received Eat")
      goto(ChoosingFood)
    }
    case Event(Abuse, _) => {
      println("Received Abuse")
      goto(Abusing)
    }
    case Event(UnknownCommand, Uninitialized) => {
      //TODO how to add hmmm... what?
      goto(Serving)
    }
  }

  when(ChoosingFood) {
    case _ => goto(Serving)
  }

  when(Abusing) {
    case Event(BotHearsText(text), Uninitialized) => {
      println("Receieved BotHearsText " + text)
      goto(Serving) using UserSaid(text)
    }
    case _ => goto(Serving)
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

  }

  initialize()
}
