package org.kirhgoff.lastobot

import akka.actor.{ActorSystem, Props, ActorRef}
import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
class LastobotApp(val userInputProcessor: ActorRef) extends TelegramBot(Utils.tokenFromFile("/Users/kirilllastovirya/lastobot.token"))
  with Polling with Commands {

  val states =
    on("obey") { (sender, args) =>
      replyTo(sender) {
        userInputProcessor ! CommandReceived(sender, "obey", args)
        "Yees, my master!"
      }
    }

  on("eat") { (sender, args) =>
    val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
      Array(Array("bread", "meat", "vegetables")),
      resizeKeyboard = true,
      oneTimeKeyboard = true
    )
    sendMessage(sender, "What food may I serve you, my master?", None, None, None, Option(keyboard))
    userInputProcessor ! CommandReceived(sender, "eat", args)

  }

  on("abuse") { (sender, args) =>
    val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
      Array(Array("да...", "нет...")),
      resizeKeyboard = true,
      oneTimeKeyboard = true
    )
    sendMessage(sender, "Да?", None, None, None, Option(keyboard))
    userInputProcessor ! CommandReceived(sender, "abuse", args)
  }

  override def onText(text: String, message: Message): Unit = text match {
    case "да..." => {
      sendMessage(message.chat.id, "Манда")
      userInputProcessor ! TextReceived(message)
    }
    case _ => {
      super.onText(text, message)
      userInputProcessor ! TextReceived(message)
    }
  }
}

object LastobotApp {
  def main(args: Array[String]): Unit = {
    println("Lastobot starting")
    val system  = ActorSystem(s"Lastobot")
    val userInputReceiver = system.actorOf(Props[UserInputProcessor], name = "userInput")
    new LastobotApp(userInputReceiver).run()
  }
}

