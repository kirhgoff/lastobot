package org.kirhgoff.lastobot

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

import scala.concurrent.duration.{DurationInt, Duration}

class RobotFSMTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll {
  def this() = this(ActorSystem("LastobotSpec"))

  override def afterAll(): Unit = {
    system.terminate()
  }

  "RobotFSM actor" - {
    "should be uninitialized" in {
      val bot = TestFSMRef[State, Data, RobotFSM](new RobotFSM(1))
      bot.stateName should be(Serving)
      bot.stateData should be(Uninitialized)
    }

    "should abuse correctly" in {
      val bot = TestFSMRef[State, Data, RobotFSM](new RobotFSM(1))
      bot.stateName should be(Serving)
      bot.stateData should be(Uninitialized)

      bot ! Abuse
      bot.stateName should be(Abusing)
      bot.stateData should be(Uninitialized)
      //TODO why it does not work
      //expectMsgAnyClassOf[Keyboard]()
      receiveOne(1 seconds)

      bot ! BotHearsText("да")
      expectMsg(Text(1, "Манда!"))
      bot.stateName should be(Serving)

      bot ! Abuse
      bot.stateName should be(Abusing)
    }
  }

}
