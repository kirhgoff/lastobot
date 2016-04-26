package org.kirhgoff.lastobot

import info.mukel.telegram.bots.api.Message

/**
  * Created by kirilllastovirya on 26/04/2016.
  */

//What Telegram bot receives
trait InputEvents
case class CommandReceived (sender:Int, commandName:String, args:Seq[String]) extends InputEvents
case class TextReceived (msg:Message) extends InputEvents
