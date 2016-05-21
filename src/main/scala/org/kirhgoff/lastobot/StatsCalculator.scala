package org.kirhgoff.lastobot

/**
  * Created by kirilllastovirya on 21/05/2016.
  */
object StatsCalculator {
  //http://hotmath.com/hotmath_help/topics/line-of-best-fit.html
  def bestFit (values:List[(Long, Double)]):(Double, Double) = {
    val n = values.length
    //we start from one
    val (overallX, overallY) = values.foldLeft((0d, 0d)) { case ((accX, accY), (x, y)) => (accX + x, accY + y)}
    val (meanX, meanY) = (overallX/n, overallY/n)

    val squares = values.map {case (x, _) => (x - meanX) * (x - meanX)}.sum
    val slope = values.map { case(x, y) => (x - meanX) * (y - meanY)}.sum / squares
    val shift = meanY - slope*meanX
    (slope, shift)
  }
}
