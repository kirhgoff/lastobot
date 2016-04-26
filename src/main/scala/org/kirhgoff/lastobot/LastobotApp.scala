package org.kirhgoff.lastobot

import info.mukel.telegram.bots.OptionPimps._
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

trait State
case object Obeying extends State
case object Eating extends State
case object Abusing extends State

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
object LastobotApp extends TelegramBot(Utils.tokenFromFile("/Users/kirilllastovirya/lastobot.token"))
  with Polling with Commands {
  val states =
  on("obey") { (sender, args) =>
    replyTo(sender) {
      "Yees, my master!"
    }
  }

  on("eat") { (sender, _) =>
    val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
      Array(Array("bread", "meat", "vegetables")),
      resizeKeyboard = true,
      oneTimeKeyboard = true
    )
    sendMessage(sender, "What food may I serve you, my master?", None, None, None, Option(keyboard))

  }

  on("abuse") { (sender, _) =>
    val keyboard: ReplyMarkup = new ReplyKeyboardMarkup(
      Array(Array("да...", "нет...")),
      resizeKeyboard = true,
      oneTimeKeyboard = true
    )
    sendMessage(sender, "Да?", None, None, None, Option(keyboard))
  }

  override def onText(text: String, message: Message): Unit = text match {
    case "да..." => sendMessage(message.chat.id, "Манда")
    case _ => super.onText(text, message)
  }

  def main(args: Array[String]): Unit = {
    println("Lastobot starting")
    LastobotApp.run()
  }
}

