package org.kirhgoff.lastobot

import org.scalatest.{FreeSpecLike, Matchers}

/**
  * Created by kirilllastovirya on 9/05/2016.
  */
class PhraseTest extends Matchers with FreeSpecLike{
  "Phrase" - {
    "should be able to use partial function" in {
      implicit val locale = Russian
      val partial = Phrase.russian("trrr")
      partial.apply(Russian) should equal("trrr")
    }

    "should be able to use string comprehension" in {
      implicit val locale = Russian
      val partial = Phrase.russian("x=%d", 3)
      partial.apply(Russian) should equal("x=3")

      val partial2 = Phrase.english("y=%d", 5)
      partial2.apply(English) should equal("y=5")

      val compose = partial orElse partial2
      compose.apply(Russian) should equal("x=3")
      compose.apply(English) should equal("y=5")
    }

    "should work in say yes for english" in {
      implicit val locale:BotLocale = English
      Phrase.sayYes.contains("yes") should equal(true)
    }

    "should work in say yes for russian" in {
      implicit val locale:BotLocale = Russian
      Phrase.sayYes.contains("да") should equal(true)
    }

  }

}
