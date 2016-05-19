package org.kirhgoff.lastobot

/**
  * Created by kirilllastovirya on 19/05/2016.
  */
class ChartActor {

}

object ChartTest extends scalax.chart.module.Charting {
  def main(args: Array[String]) {
    val data = for (i <- 1 to 5) yield (i,i)
    val chart = XYLineChart(data)
    chart.saveAsPNG("/tmp/chart.png")
  }
}
