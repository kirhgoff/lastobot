package org.kirhgoff.lastobot

import akka.actor.Actor
import info.mukel.telegram.bots.api.Message

/**
  * Created by kirilllastovirya on 26/04/2016.
  *
  * Class processes requests received from user and converts them
  * into internal bot events, creating bots per sender
  */
class UserInputProcessor extends Actor{
  override def receive: Receive = {
    case CommandReceived(sender, commandName, args) ⇒
      println(s"Command received: $commandName")
    case TextReceived(msg:Message) =>
      println(s"Text receievd: ${msg.text}")
  }
}
