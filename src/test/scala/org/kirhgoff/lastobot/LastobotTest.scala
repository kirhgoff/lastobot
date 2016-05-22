package org.kirhgoff.lastobot

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.kirhgoff.lastobot.Command._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpecLike, Matchers}

class LastobotTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll with BeforeAndAfter {
  def this() = this(ActorSystem("LastobotSpec"))
  val userStorage = new StorageBotFactory("localhost", 27017).userStorageFor(666)

  override def afterAll(): Unit = {
    system.terminate()
    userStorage.db.dropDatabase()
  }

  before {
    userStorage.db.dropDatabase()
  }

  "RobotFSM actor" - {
    "should be uninitialized" in {
      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(1, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)
    }

    "should take care of smoking" in {
      userStorage.clearSmokes()
      userStorage.smokedOverall() should equal(0)

      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! Smoke(42)
      bot.stateName should be(ConfirmingSmoke)
      bot.stateData should be(UserSmoked(42))

      expectMsgType[Keyboard]

      bot ! UserSaid("да")

      bot.stateName should be(Serving)
      bot.stateData should be(Yes)

      userStorage.smokedOverall() should equal(42)

      bot.stateName should be(Serving)
    }

    "should be able to check locale" in {
      userStorage.updateLocale(English)
      userStorage.getLocaleOr(Russian) should equal(English)

      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! ChangeLocale

      expectMsgType[Keyboard]

      bot.stateName should be(ChangingLocale)
      bot.stateData should be(Empty)

      bot ! UserSaid("Русский")

      bot.stateName should be(Serving)
      bot.stateData should be(UserChangedLocale(Russian))

      userStorage.getLocaleOr(English) should equal(Russian)

      bot.stateName should be(Serving)
    }

    "should be able to send start message" in {
      userStorage.updateLocale(English)
      userStorage.getLocaleOr(Russian) should equal(English)

      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! Start

      val msg: Text = expectMsgType[Text]
      msg.sender should equal(666)
      msg.text should equal(Phrase.intro(English))

      bot.stateName should be(Serving)
    }

    "should be able to send stats for smoking" in {
      val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))
      bot.stateName should be(Serving)
      bot.stateData should be(Empty)

      bot ! SmokingStats
      expectMsgType[Text] match {
        case Text(senderId, str) =>
          senderId should equal (666)
          str should equal (Phrase.noDataYet(English))
        case _ => fail()
      }
      bot.stateName should be(Serving)

      userStorage.smoked(2)
      bot ! SmokingStats

      val msg1: Picture = expectMsgType[Picture]
      msg1.sender should equal(666)
      msg1.filePath should include ("/bot")

      val msg2: Picture = expectMsgType[Picture]
      msg2.sender should equal(666)
      msg2.filePath should include ("/bot")

      bot.stateName should be(Serving)
    }
  }
}
