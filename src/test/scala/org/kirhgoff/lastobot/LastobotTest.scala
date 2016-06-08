package org.kirhgoff.lastobot

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.kirhgoff.lastobot.BotAction._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpecLike, Matchers}

class LastobotTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with FreeSpecLike with BeforeAndAfterAll with BeforeAndAfter {
  def this() = this(ActorSystem("LastobotSpec"))

  private val factory = new StorageBotFactory("localhost", 27017)
  private val userStorage = factory.userStorageFor(666)
  private val bot = TestFSMRef[State, Data, SmokeBot](new SmokeBot(666, userStorage))

  override def afterAll(): Unit = {
    system.terminate()
    factory.close()
  }

  before {
    userStorage.db.dropDatabase()
    bot ! Reset
    assertState(Serving, Empty)
  }

  after{
    assertState(Serving, Empty)
  }

  //TODO add test with What? cases

  "RobotFSM actor" - {
    "should take care of smoking if user types directly" in {
      userStorage.clearSmokes()
      userStorage.smokedOverall() should equal(0)

      bot ! Smoke(Some("42"))
      expectMsgType[Text]

      userStorage.smokedOverall() should equal(42)
    }

    "should take care of smoking if user not entering anything" in {
      userStorage.clearSmokes()
      userStorage.smokedOverall() should equal(0)

      bot ! Smoke(None)
      expectMsgType[Text] //how many cigarettes
      assertState(GettingSmoked, Empty)

      bot ! UserSaid("24")
      userStorage.smokedOverall() should equal(24)

      expectMsgType[Text] //you smoked 24 cigarettes
    }

    "should be able to check locale" in {
      setLocale(English)

      bot ! ChangeLocale
      assertState(ChangingLocale, Empty)

      expectMsgType[Keyboard]

      bot ! UserSaid("Русский")
      userStorage.getLocaleOr(English) should equal(Russian)
    }

    "should be able to send start message" in {
      bot ! Start
      expectText(Phrase.intro(English)) //By default locale is English
    }

    "should be able to send stats for smoking" in {
      bot ! ShowSmokingStats
      expectText(Phrase.noDataYet(English))

      assertState(Serving, Empty)

      userStorage.smoked(2)
      bot ! ShowSmokingStats

      //Expect 2 pictures
      expectPicture()
      expectPicture()
    }

    "save weight if user provided a number" in {
      bot !  Weight(Some("45.0"))
      expectTextWith("45.0") //
      userStorage.lastWeight() should equal (Some(45.0))
    }

    "ask what if user provided blah instead of number" in {
      bot !  Weight(Some("blah"))
      expectMsgType[Text] //what?
    }

    "ask for weight if user just typed weight" in {
      bot !  Weight(None)
      expectMsgType[Text] //Type the weight

      bot !  UserSaid("55.0")
      userStorage.lastWeight() should equal (Some(55.0))
    }

    "ask for weight if user just typed weight and he typed blah" in {
      bot !  Weight(None)
      expectMsgType[Text] //Type the weight

      bot !  UserSaid("blah")
      expectMsgType[Text] //what?
    }

  }

  def setLocale(newLocale: BotLocale): Unit = {
    userStorage.updateLocale(newLocale)
    userStorage.getLocaleOr(getOpposite(newLocale)) should equal(newLocale)
  }

  def getOpposite(newLocale: BotLocale) = newLocale match {
    case English => Russian
    case Russian => English
  }

  def expectPicture(): Unit = {
    val msg1: Picture = expectMsgType[Picture]
    msg1.sender should equal(666)
    msg1.filePath should include("/bot")
  }

  def expectText(text: String): Unit = {
    val msg: Text = expectMsgType[Text]
    msg.sender should equal(666)
    msg.text should equal(text)
  }

  def expectTextWith(substring: String): Unit = {
    val msg: Text = expectMsgType[Text]
    msg.text should include(substring)
  }

  def assertState(currentState: State, smoked: Data): Unit = {
    bot.stateName should be(currentState)
    bot.stateData should be(smoked)
  }
}
