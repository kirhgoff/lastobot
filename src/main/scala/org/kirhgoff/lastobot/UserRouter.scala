package org.kirhgoff.lastobot

import akka.actor.{Actor, ActorRef, Props}
import info.mukel.telegram.bots.TelegramBot
import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}

import scala.collection.mutable
import scala.util.Try

/**
  * Created by kirilllastovirya on 26/04/2016.
  *
  * Class processes requests received from user and converts them
  * into internal bot events, creating bots per sender
  */
class UserRouter(val bot:TelegramBot) extends Actor {
  val senderMap = mutable.Map[Int, ActorRef]()
  val storageFactory = new StorageBotFactory ("localhost", 27017)

  override def receive: Receive = {
    case UserCommand(sender, commandName, args) ⇒ commandName match {
      case "obey" => senderActor (sender) ! Command.Obey
      case "eat" => senderActor (sender) ! Command.Eat
      case "abuse" => senderActor (sender) ! Command.Abuse
      case "smoke" => senderActor (sender) ! Command.Smoke(args.headOption.getOrElse("1").toInt)
      case any => println("Unknown command " + any)
    }
    case UserTextMessage(msg:Message) => {
      senderActor(msg.chat.id) ! UserSaid(msg.text.getOrElse("blah"))
    }

    //Feedback
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
    val userStorage: UserStorage = storageFactory.userStorageFor(senderId)
    senderMap.getOrElseUpdate(senderId,
      context.actorOf(Props(new Lastobot(senderId, userStorage)), name = s"Sender$senderId")
    )
  }

  override def postStop() = storageFactory.close
}