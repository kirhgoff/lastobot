package org.kirhgoff.lastobot

import akka.actor.{ActorSystem, Props, ActorRef}
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

import info.mukel.telegram.bots.OptionPimps._

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
class LastobotApp extends TelegramBot(Utils.tokenFromFile("/Users/kirilllastovirya/lastobot.token"))
  with Polling with Commands {

  val system  = ActorSystem(s"Lastobot")
  var userInputProcessor = system.actorOf(Props(new UserInputProcessor(this)),
    name = "userInput")

  val states = {
    on("eat") { (sender, args) =>
      userInputProcessor ! UserCommand(sender, "eat", args)
    }
    on("obey") { (sender, args) =>
      userInputProcessor ! UserCommand(sender, "obey", args)
    }
    on("abuse") { (sender, args) =>
      userInputProcessor ! UserCommand(sender, "abuse", args)
    }

  }
  override def onText(text: String, message: Message): Unit =
    userInputProcessor ! UserText(message)
}

object LastobotApp {
  def main(args: Array[String]): Unit = {
    println("Lastobot starting")
    new LastobotApp().run()
  }
}

