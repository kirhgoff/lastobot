package org.kirhgoff.lastobot

/**
  * Created by kirilllastovirya on 21/05/2016.
  */
object StatsCalculator {
  //http://hotmath.com/hotmath_help/topics/line-of-best-fit.html
  def bestFit (valuesY:List[Double]):Double = {
    val n = valuesY.length
    //we start from one
    val valuesX = 1 to n
    val meanX = valuesY.sum / n
    val meanY = n * (n + 1) / 2

    val squares = valuesX.map(x => (x - meanX) * (x - meanX)).sum
    (valuesX, valuesY).zipped.map((x, y) => (x - meanX) * (y - meanY)).sum / squares
  }
}
