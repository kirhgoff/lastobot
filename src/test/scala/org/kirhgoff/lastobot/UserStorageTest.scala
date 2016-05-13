package org.kirhgoff.lastobot

import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

/**
  * Created by kirilllastovirya on 13/05/2016.
  */
class UserStorageTest  extends Matchers with FreeSpecLike  with BeforeAndAfterAll{
  val userStorage = new StorageBotFactory("localhost", 27017).userStorageFor(666)

  override def afterAll(): Unit = {
    userStorage.db.dropDatabase()
  }

  "UserStorage" - {
    "should be able to save/get locale" in {
      userStorage.getLocaleOr(English) should equal(English)
      userStorage.updateLocale(Russian) should equal(Russian)
      userStorage.getLocaleOr(English) should equal(Russian)
      userStorage.updateLocale(English) should equal(English)
    }
  }
}
