package org.kirhgoff.lastobot

import info.mukel.telegram.bots.api.{ReplyMarkup, ReplyKeyboardMarkup, ChatAction}
import info.mukel.telegram.bots.{Utils, Commands, Polling, TelegramBot}

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
object LastobotApp {
  def main(args: Array[String]): Unit = {
    println("Starting bot")
    val TOKEN = ""

    object MyBot extends TelegramBot(Utils.tokenFromFile("/Users/kirilllastovirya/lastobot.token"))
      with Polling with Commands {

      on("obey") { (sender, args) =>
        replyTo(sender) {
          "Yees, my master!"
        }
      }

      on("eat") { (sender, _) =>
        val keyboard:ReplyMarkup = new ReplyKeyboardMarkup(
          Array(Array("food", "booze")),
          None, Some(true), None
        )
        sendMessage(sender, "What food do you want?", None, None, None, Option(keyboard))
      }

      on("picture") { (sender, _) =>
        val keyboard:ReplyMarkup = new ReplyKeyboardMarkup(
          Array(Array("food", "booze")),
          None, Some(true), None
        )
        sendMessage(sender, "What food do you want?", None, None, None, Option(keyboard))
      }

    }

    MyBot.run()
  }
}

