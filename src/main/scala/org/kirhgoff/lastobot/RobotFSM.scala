package org.kirhgoff.lastobot

import akka.actor.FSM

//received commands
final case class Obey()
final case class Eat()
final case class Abuse()
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
final case class UserSaid(val text:String) extends Data

/**
  * Created by kirilllastovirya on 26/04/2016.
  */
class RobotFSM(val senderId: Int) extends FSM[State, Data] {

  startWith(Serving, Uninitialized)

  when(Serving) {
    case Event(Obey, Uninitialized) => {
      sender() ! Text(senderId, "Yes, my master!")
      goto(Serving)
    }
    case Event(Eat, Uninitialized) => {
      goto(ChoosingFood)
    }
    case Event(Abuse, Uninitialized) => {
      goto(Abusing)
    }
    case Event(UnknownCommand, Uninitialized) => {
      //TODO how to add hmmm... what?
      goto(Serving)
    }
    case Event(BotHearsText(text), Uninitialized) => {
      goto(Serving) using UserSaid(text)
    }
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
    case Abusing -> Serving => stateData match {
      case UserSaid(text) if text.startsWith("да") =>
        sender() ! Text(senderId, "Манда!")
      case _ =>
    }

  }

  initialize()
}
