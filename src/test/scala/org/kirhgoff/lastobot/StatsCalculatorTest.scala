package org.kirhgoff.lastobot

import org.scalatest.{FreeSpecLike, Matchers}

/**
  * Created by kirilllastovirya on 21/05/2016.
  */
class StatsCalculatorTest extends Matchers with FreeSpecLike {
    "StatsCalculator" - {
      "should be able to calculate best fit" in {
        StatsCalculator.bestFit(List(1, 2, 3)) should equal(1)
        StatsCalculator.bestFit(List(3, 2, 1)) should equal(-1)
      }
    }
}

