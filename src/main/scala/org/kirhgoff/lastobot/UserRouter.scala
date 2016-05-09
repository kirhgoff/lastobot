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
      case "obey" => userActor (sender) ! Command.Obey
      case "eat" => userActor (sender) ! Command.Eat
      case "abuse" => userActor (sender) ! Command.Abuse
        //TODO proper int parsing
      case "smoke" => userActor (sender) ! Command.Smoke(args.headOption.getOrElse("1").toInt)
      case "stats" => userActor (sender) ! Command.SmokingStats
      case "start" => userActor (sender) ! Command.Start
      case any => println("Unknown command " + any)
    }
    case UserTextMessage(msg:Message) => {
      userActor(msg.chat.id) ! UserSaid(msg.text.getOrElse("blah"))
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

  def userActor(senderId: Int): ActorRef = {
    val userStorage: UserStorage = storageFactory.userStorageFor(senderId)
    senderMap.getOrElseUpdate(senderId,
      context.actorOf(Props(new SmokeBot(senderId, userStorage)), name = s"Sender$senderId")
    )
  }

  override def postStop() = storageFactory.close
}
