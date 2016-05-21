package org.kirhgoff.lastobot

import java.util

import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.{BitmapEncoder, CategoryChartBuilder}
import org.knowm.xchart.style.Styler.LegendPosition


/**
  * Created by kirilllastovirya on 19/05/2016.
  */
class ChartActor {

}

object ChartTest extends {
  //http://knowm.org/open-source/xchart/xchart-example-code/
  def main(args: Array[String]) {
    import scala.collection.JavaConverters._

    // Create Chart
    val chart = new CategoryChartBuilder()

      .width(200)
      .height(200)
//      .title("Score Histogram")
//      .xAxisTitle("Score")
//      .yAxisTitle("Number")
      .build()

    // Customize Chart
    chart.getStyler.setLegendVisible(false)
    chart.getStyler.setPlotContentSize(1.0)

    // Series
    val javaList: util.List[Integer] = List(0, 1, 2, 3, 4).map(Integer.valueOf).asJava
    val javaList2: util.List[Integer] = List(5, 6, 9, 1, 2).map(Integer.valueOf).asJava
    chart.addSeries("test 1", javaList, javaList2)

    BitmapEncoder.saveBitmap(chart, "/tmp/chart", BitmapFormat.PNG)
  }
}

object ScalaCharts extends scalax.chart.module.Charting {

  def barChart: Unit = {
    implicit val theme = org.jfree.chart.StandardChartTheme.createDarknessTheme
    val data = for (i <- 1 to 5) yield (i, i)
    val chart = BarChart(data, title = "Weekly")
    chart.saveAsPNG("/tmp/chart.png")
  }

  def lineChart(): Unit = {
    val data = for (i <- 1 to 5) yield (i, i)
    val chart = XYLineChart(data)
    chart.saveAsPNG("/tmp/chart.png")
  }
}
