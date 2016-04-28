package org.kirhgoff.lastobot

import info.mukel.telegram.bots.api.Message

/**
  * Created by kirilllastovirya on 26/04/2016.
  */

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserText(msg:Message) extends UserMessages

//What FSM receives
trait BotMessages
case class BotHearsCommand (commandName:String, args:Seq[String]) extends BotMessages
case class BotHearsText (text:String) extends BotMessages


