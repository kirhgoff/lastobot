package org.kirhgoff.lastobot

import java.time.LocalDate

import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

/**
  * Created by kirilllastovirya on 13/05/2016.
  */
class UserStorageTest  extends Matchers with FreeSpecLike  with BeforeAndAfterAll{
  import DateConversions._
  val userStorage = new StorageBotFactory("localhost", 27017).userStorageFor(666)

  override def beforeAll(): Unit = {
    userStorage.db.dropDatabase()
  }

  "UserStorage" - {
    "should be able to save/get locale" in {
      userStorage.getLocaleOr(English) should equal(English)
      userStorage.updateLocale(Russian) should equal(Russian)
      userStorage.getLocaleOr(English) should equal(Russian)
      userStorage.updateLocale(English) should equal(English)
    }

    "should be able to give raw data" in {
      //3, 2, 1
      List(19, 18, 18, 17, 17, 17).foreach(
        day => userStorage.smoked(1, LocalDate.of(2016, 5, day))
      )
      val checkDate = LocalDate.of(2016, 5, 18)
      val epochDate = checkDate.toEpochDay
      userStorage.aggregatedByDateBefore(checkDate) should equal(List(
        (epochDate + 1, 1d),
        (epochDate, 2d)
      ))
    }
  }

}
