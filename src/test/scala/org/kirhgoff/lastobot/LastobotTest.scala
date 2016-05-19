package org.kirhgoff.lastobot

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.kirhgoff.lastobot.Command._
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
      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(1, null))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)
    }

//    "should obey" in {
//      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(1, null))
//      bot.stateName should be(Serving)
//      bot.stateData should be(Empty)
//
//      bot ! Obey
//      expectMsg(Text(1, "Yes, my master!"))
//      bot.stateName should be(Serving)
//    }
//
//    "Keyboards should match" in {
//      val keyboard1 = Keyboard(666,
//        "What food may I serve you, my master?",
//        Array(Array("bread", "butter", "beer")))
//
//      val keyboard2 = Keyboard(666,
//        "What food may I serve you, my master?",
//        Array(Array("bread", "butter", "beer")))
//
//      keyboard1 should equal(keyboard2)
//      keyboard2 should equal(keyboard1)
//    }
//
//    "should feed master" in {
//      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, null))
//      bot.stateName should be(Serving)
//      bot.stateData should be(Empty)
//
//      bot ! Eat
//      expectMsg(Keyboard(666,
//        "What food may I serve you, my master?",
//        Array(Array("bread", "butter", "beer"))))
//      bot.stateName should be(Serving)
//    }
//
//
//    "should abuse correctly" in {
//      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, null))
//      bot.stateName should be(Serving)
//      bot.stateData should be(Empty)
//
//      bot ! Abuse
//      bot.stateName should be(Abusing)
//      bot.stateData should be(Empty)
//      //TODO why it does not work
//      //expectMsgAnyClassOf[Keyboard]()
//      expectMsg(Keyboard(666,
//        "Скажи \"да\"",
//        Array(Array("да", "нет"))))
//
//      bot ! UserSaid("да")
//      expectMsg(Text(666, "Манда!"))
//      bot.stateName should be(Serving)
//
//      bot ! Abuse
//      bot.stateName should be(Abusing)
//      expectMsg(Keyboard(666,
//        "Скажи \"да\"",
//        Array(Array("да", "нет"))))
//      bot ! UserSaid("нет")
//      bot.stateName should be(Serving)
//      expectNoMsg()
//    }

    "should take care of smoking" in {
      userStorage.clear()
      userStorage.smokedOverall() should equal(0)

      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))
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
