package org.kirhgoff.lastobot

import akka.actor.{ActorSystem, Props, ActorRef}
import info.mukel.telegram.bots.api.{Message, ReplyKeyboardMarkup, ReplyMarkup}
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

import info.mukel.telegram.bots.OptionPimps._

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
class LastobotApp(token:String) extends TelegramBot(token)
  with Polling with Commands {

  val system  = ActorSystem(s"Lastobot")
  var userRouter = system.actorOf(Props(new UserRouter(this)),
    name = "userRouter")

  val states = {
    List("start", "eat", "obey", "abuse", "smoke", "stats").foreach(
      c  => on(c) { (sender, args) => {
          println(s"Sending user command: $c")
          userRouter ! UserCommand(sender, c, args)
        }
      }
    )
  }
  override def onText(text: String, message: Message): Unit =
    userRouter ! UserTextMessage(message)

  def terminate = {
    system.terminate()
  }
}

object LastobotApp {
  def main(args: Array[String]): Unit = {
    println("lastobot v1.0 is running")
    val app: LastobotApp = new LastobotApp(Utils.tokenFromFile(args(0)))
    app.run
    //TODO how to terminate correctly
    app.terminate
  }
}

