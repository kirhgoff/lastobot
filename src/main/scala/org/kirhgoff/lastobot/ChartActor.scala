package org.kirhgoff.lastobot

import java.util

import com.mongodb.casbah.Imports._
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import org.knowm.xchart._
import org.knowm.xchart.style.Styler.LegendPosition

import scala.util.Random


/**
  * Created by kirilllastovirya on 19/05/2016.
  */
class ChartActor {

}

object ChartTest extends {

  //http://knowm.org/open-source/xchart/xchart-example-code/
  def main(args: Array[String]) {

//    val mongoClient = MongoClient("localhost", 27017)
//    val db = mongoClient("sender_3")
//    val userStorage = new UserStorage(db)
    //val results = userStorage.aggregatedByDateBefore(LocalDate.of(2016, 5, 14))

    val random = new Random()
    val dots = (for (x <- 1 to 30; y = random.nextInt(30))
      yield (x.toDouble, y.toDouble)).toList

    val chart: XYChart = ScalaCharts.monthlyCigarettesChart(dots)

    BitmapEncoder.saveBitmap(chart, "/tmp/chart", BitmapFormat.PNG)
  }

}

object ScalaCharts extends scalax.chart.module.Charting {
  import scala.collection.JavaConverters._

  def monthlyCigarettesChart(dots:List[(Double, Double)]): XYChart = {
    val (days: List[Double], values: List[Double]) = dots.unzip

    val chart = new XYChartBuilder()
      .width(400)
      .height(600)
      .title("Month results")
      .xAxisTitle("Days before")
      .yAxisTitle("Cigarettes")
      .theme(org.knowm.xchart.style.Styler.ChartTheme.GGPlot2)
      .build()

    //chart.getStyler.setLegendVisible(false)
    chart.getStyler.setPlotContentSize(1.0)
    chart.getStyler.setYAxisDecimalPattern("###.#")
    chart.getStyler.setLegendPosition(LegendPosition.InsideN)
    chart.getStyler.setYAxisMax(values.max * 1.25)

    //TODO wtf?!
    val daysJava = listToJava(days)
    val valuesJava = listToJava(values)
    chart.addSeries("monthly", daysJava, valuesJava)

    // Trend line
    val (slope:Double, shift:Double) = StatsCalculator.bestFit(dots)
    def trendLine (x:Double) = slope*x + shift

    val (xStart, xEnd) = minMax(days)
    val series = chart.addSeries(
      "trend: " + slope,
      List(xStart, xEnd),
      List(trendLine(xStart), trendLine(xEnd))
    )
    series.setChartXYSeriesRenderStyle(Line)

    chart
  }

  def minMax(days: List[Double]): (Double, Double) = {
    val (xStart, xEnd) = days.foldLeft((days.head, days.head)) {
      case ((min, max), e) => (math.min(min, e), math.max(max, e))
    }
    (xStart, xEnd)
  }

  implicit def listToJava(list: List[Double]): util.List[Number] = {
    list.map(_.asInstanceOf[Number]).asJava
  }

  def weeklyCigarettesChart(days: List[String], values: List[Double]): CategoryChart = {
    //TODO weird import issue - move out from the method
    //TODO use i18n
    val chart = new CategoryChartBuilder()
      .width(400)
      .height(600)
      .title("Week results")
      .xAxisTitle("Days")
      .yAxisTitle("Cigarettes")
      .theme(org.knowm.xchart.style.Styler.ChartTheme.GGPlot2)
      .build()

    chart.getStyler.setLegendVisible(false)
    chart.getStyler.setPlotContentSize(1.0)
    chart.getStyler.setYAxisDecimalPattern("###.#")

    createSeries(chart, "cigarettes", days, values)
  }

  def createSeries(chart: CategoryChart, label: String, keys: List[String], values: List[Double]): CategoryChart = {
    val daysJava = keys.asJava
    val valuesJava: util.List[Number] = values.map(x => x.asInstanceOf[Number]).asJava
    chart.addSeries(label, daysJava, valuesJava)
    chart
  }

  def lineChart(): Unit = {
    val data = for (i <- 1 to 5) yield (i, i)
    val chart = XYLineChart(data)
    chart.saveAsPNG("/tmp/chart.png")
  }
}
