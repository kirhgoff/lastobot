package org.kirhgoff.lastobot

import org.scalatest.{FreeSpecLike, Matchers}

/**
  * Created by kirilllastovirya on 21/05/2016.
  */
class StatsCalculatorTest extends Matchers with FreeSpecLike {
    "StatsCalculator" - {
      "should be able to calculate best fit" in {
        StatsCalculator.bestFit(List((1L, 1d), (2L, 2d), (3L, 3d))) should equal(1d, 0d)
        StatsCalculator.bestFit(List((1L, 3d), (2L, 2d), (3L, 1d))) should equal(-1d, 4d)
      }
    }
}

