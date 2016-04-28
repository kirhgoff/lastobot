package org.kirhgoff.lastobot

import akka.actor.{ActorSystem, Props, ActorRef}
import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
class LastobotApp extends TelegramBot(Utils.tokenFromFile("/Users/kirilllastovirya/lastobot.token"))
  with Polling with Commands {
  val system  = ActorSystem(s"Lastobot")
  var userInputProcessor = system.actorOf(Props(new UserInputProcessor(this)),
    name = "userInput")

  val states =
    on("obey") { (sender, args) =>
      replyTo(sender) {
        userInputProcessor ! UserCommand(sender, "obey", args)
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
    userInputProcessor ! UserCommand(sender, "eat", args)

  }

  on("abuse") { (sender, args) =>
    val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
      Array(Array("да...", "нет...")),
      resizeKeyboard = true,
      oneTimeKeyboard = true
    )
    sendMessage(sender, "Да?", None, None, None, Option(keyboard))
    userInputProcessor ! UserCommand(sender, "abuse", args)
  }

  override def onText(text: String, message: Message): Unit = text match {
    case "да..." => {
      sendMessage(message.chat.id, "Манда")
      userInputProcessor ! UserText(message)
    }
    case _ => {
      super.onText(text, message)
      userInputProcessor ! UserText(message)
    }
  }
}

object LastobotApp {
  def main(args: Array[String]): Unit = {
    println("Lastobot starting")
    new LastobotApp().run()
  }
}

