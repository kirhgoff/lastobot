package org.kirhgoff.lastobot

import akka.actor.{Props, ActorRef, Actor}
import info.mukel.telegram.bots.TelegramBot
import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.api.{ReplyKeyboardMarkup, ReplyMarkup, Message}

import scala.collection.mutable

/**
  * Created by kirilllastovirya on 26/04/2016.
  *
  * Class processes requests received from user and converts them
  * into internal bot events, creating bots per sender
  */
class UserInputProcessor(val bot:TelegramBot) extends Actor {
  val senderMap = mutable.Map[Int, ActorRef]()
  override def receive: Receive = {
    case UserCommand(sender, commandName, args) ⇒ commandName match {
      case "obey" => senderActor (sender) ! Obey
      case "eat" => senderActor (sender) ! Eat
      case "abuse" => senderActor (sender) ! Abuse
      case _ => senderActor (sender) ! UnknownCommand
    }
    case UserText(msg:Message) => {
      senderActor(msg.chat.id) ! BotHearsText(msg.text.toString)
    }

    //Receives from bot
    case Text(sender:Int, text:String) => {
      bot.sendMessage(sender, text)
    }
    case Keyboard(sender:Int, text:String, buttons:Array[Array[String]]) => {
      val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
        buttons,
        resizeKeyboard = true,
        oneTimeKeyboard = true
      )
      bot.sendMessage(sender, text, None, None, None, Option(keyboard))
    }
  }

  def senderActor(senderId: Int): ActorRef = {
    senderMap.getOrElseUpdate(senderId,
      context.actorOf(Props(new RobotFSM(senderId)), name = s"Sender$senderId")
    )
  }
}
