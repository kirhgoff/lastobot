package org.kirhgoff.lastobot

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.kirhgoff.lastobot.Command.{Abuse, Smoke, SmokingStats}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

import scala.concurrent.duration.{Duration, DurationInt}

class LastobotTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll {
  def this() = this(ActorSystem("LastobotSpec"))
  val userStorage = new StorageBotFactory("localhost", 27017).userStorageFor(666)

  override def afterAll(): Unit = {
    system.terminate()
    userStorage.db.dropDatabase()
  }

  "RobotFSM actor" - {
    "should be uninitialized" in {
      val bot = TestFSMRef[State, Data, Lastobot](new Lastobot(1, null))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)
    }

    "should abuse correctly" in {
      val bot = TestFSMRef[State, Data, Lastobot](new Lastobot(1, null))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! Abuse
      bot.stateName should be(Abusing)
      bot.stateData should be(Empty)
      //TODO why it does not work
      //expectMsgAnyClassOf[Keyboard]()
      receiveOne(1 seconds)

      bot ! UserSaid("да")
      expectMsg(Text(1, "Манда!"))
      bot.stateName should be(Serving)

      bot ! Abuse
      bot.stateName should be(Abusing)
    }

    "should take care of smoking" in {
      userStorage.clear()
      userStorage.smokedOverall() should equal(0)

      val bot = TestFSMRef[State, Data, Lastobot](new Lastobot(666, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! Smoke(42)
      bot.stateName should be(ConfirmingSmoke)
      bot.stateData should be(UserSmoked(42))

      receiveOne(1 seconds)

      bot ! UserSaid("да")

      bot.stateName should be(Serving)
      bot.stateData should be(Yes)

      userStorage.smokedOverall() should equal(42)

      bot.stateName should be(Serving)
      bot ! SmokingStats

      bot.stateName should be(ShowingStats)
    }

  }

}
