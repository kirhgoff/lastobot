package org.kirhgoff.lastobot

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.LazyLogging
import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.TelegramBot
import info.mukel.telegram.bots.api.{InputFile, Message, ReplyKeyboardMarkup, ReplyMarkup}
import org.kirhgoff.lastobot.BotAction._

import scala.collection.mutable

/**
  * Created by kirilllastovirya on 26/04/2016.
  *
  * Class processes requests received from user and converts them
  * into internal bot events, creating bots per sender. Receives
  * bots replies and converts them to Telegram messages
  */
class UserRouter(val bot:TelegramBot) extends Actor with LazyLogging {
  val senderMap = mutable.Map[Int, ActorRef]()
  val storageFactory = new StorageBotFactory ("localhost", 27017)

  override def receive: Receive = {
    //TODO move out command constants
    case UserCommand(sender, commandName, args) ⇒ commandName match {
        //TODO pass option and skip confirmation if value is set
      case "smoke" => userActor (sender) ! Smoke(args.headOption)
      case "smokestats" => userActor (sender) ! ShowSmokingStats
      case "weight" => userActor (sender) ! Weight(args.headOption)
      case "weightstats" => userActor (sender) ! ShowWeightStats
      case "start" => userActor (sender) ! Start
      case "setlocale" => userActor (sender) ! ChangeLocale
      case any => logger.error(s"Unknown command $any")
    }

    case UserTextMessage(msg:Message) =>
      userActor(msg.chat.id) ! UserSaid(msg.text.getOrElse("blah"))

    //Feedback
    case Text(sender:Int, text:String) =>
      bot.sendMessage(sender, text)

    case Keyboard(sender:Int, text:String, buttons:Array[Array[String]]) =>
      bot.sendMessage(sender, text, None, None, None,
        Option(new ReplyKeyboardMarkup(
          buttons,
          resizeKeyboard = true,
          oneTimeKeyboard = true
      )))

    case Picture(sender:Int, filePath:String) =>
      logger.info(s"Received picture $filePath")
      bot.sendPhoto(sender, InputFile(filePath))
  }

  def userActor(senderId: Int): ActorRef = {
    val userStorage: UserStorage = storageFactory.userStorageFor(senderId)
    senderMap.getOrElseUpdate(senderId,
      context.actorOf(Props(new SmokeBot(senderId, userStorage)), name = s"Sender$senderId")
    )
  }

  override def postStop() = storageFactory.close()
}
