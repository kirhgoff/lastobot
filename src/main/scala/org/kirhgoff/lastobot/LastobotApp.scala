package org.kirhgoff.lastobot

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import info.mukel.telegram.bots.api.Message
import info.mukel.telegram.bots.{Commands, Polling, TelegramBot, Utils}

/**
  * Created by kirilllastovirya on 22/04/2016.
  */
class LastobotApp(token:String) extends TelegramBot(token)
  with Polling with Commands with LazyLogging {
  val system  = ActorSystem(s"Lastobot")

  var userRouter = system.actorOf(Props(new UserRouter(this)),
    name = "userRouter")

  //TODO move strings to Commands object
  val states = {
    List("start", "smoke", "smokestats", "setlocale", "weight", "weightstats", "bug", "feature").foreach(
      c  => on(c) { (sender, args) => {
          logger.info(s"Sending user command: $c")
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

object LastobotApp extends LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info ("lastobot v1.1 is running")
    val app: LastobotApp = new LastobotApp(Utils.tokenFromFile(args(0)))
    app.run
    //TODO how to terminate correctly
    app.terminate
  }
}

