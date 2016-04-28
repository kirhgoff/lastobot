package org.kirhgoff.lastobot

import akka.actor.FSM

//received messages
final case class Obey()
final case class Eat()
final case class Abuse()

//sent messages
final case class Text(sender:Int, text:String)
final case class Keyboard(sender:Int, buttons:Array[Array[String]])

//states
sealed trait State
case object Serving extends State
case object ChoosingFood extends State

//data
sealed trait Data
case object Uninitialized extends Data

/**
  * Created by kirilllastovirya on 26/04/2016.
  */
class RobotFSM(val senderId: Int) extends FSM[State, Data] {

  startWith(Serving, Uninitialized)

  when(Serving) {
    case Event(Obey, Uninitialized) => {
      sender() ! Text(senderId, "Yes, my master!")
      stay using Uninitialized
    }
    case Event(Eat, Uninitialized) => {
      sender() ! Keyboard(senderId, Array(Array("bread", "butter", "beer")))
      goto(ChoosingFood)
    }
  }

  initialize()
}
