package org.kirhgoff.lastobot

import java.nio.file.Files
import java.time.LocalDate
import java.time.format.TextStyle
import java.util
import java.util.Locale

import com.mongodb.casbah.Imports._
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import org.knowm.xchart._
import org.knowm.xchart.style.Styler.LegendPosition

import scala.util.Random

import scala.language.implicitConversions

/**
  * Created by kirilllastovirya on 19/05/2016.
  */
object ChartTest extends {

  //http://knowm.org/open-source/xchart/xchart-example-code/
  def main(args: Array[String]) {

//    val mongoClient = MongoClient("localhost", 27017)
//    val db = mongoClient("sender_3")
//    val userStorage = new UserStorage(db)
    //val results = userStorage.aggregatedByDateBefore(LocalDate.of(2016, 5, 14))

    val random = new Random()
    val dots = (for (x <- 1 to 5; y = random.nextInt(30))
      yield (x.toDouble, y.toDouble)).toList

    val chart: XYChart = ChartsBuilder.monthlyCigarettesChart(dots)
    BitmapEncoder.saveBitmap(chart, "/tmp/chart", BitmapFormat.PNG)
  }

}

object ChartsBuilder {
  val MONTHLY_WIDTH = 600
  val MONTHLY_HEIGHT = 200
  val WEEKLY_WIDTH = 600
  val WEEKLY_HEIGHT = 200

  import scala.collection.JavaConverters._

  implicit def doubleListToJavaNumberList(list: List[Double]): util.List[Number] =
    list.map(_.asInstanceOf[Number]).asJava

  implicit def stringListToJavaStringList(list: List[String]): util.List[String] = list.asJava

  def monthlyFile(data: List[(Long, Double)]): String = {
    val filePath = createTempFile
    val chart: XYChart = ChartsBuilder.monthlyCigarettesChart(
      data.map {case (x, y) => (x.toDouble, y)}
    )
    BitmapEncoder.saveBitmap(chart, filePath, BitmapFormat.PNG)
    filePath + ".png"
  }

  def weeklyFile(data: List[(Long, Double)]): String = {
    val filePath = createTempFile
    val chart: CategoryChart = ChartsBuilder.weeklyCigarettesChart(
      data.map {case (x, y) => (x.toDouble, y)}
    )
    BitmapEncoder.saveBitmap(chart, filePath, BitmapFormat.PNG)
    filePath + ".png"
  }

  def monthlyCigarettesChart(dots:List[(Double, Double)]): XYChart = {
    val (days: List[Double], values: List[Double]) = dots.unzip

    val chart = new XYChartBuilder()
      .width(MONTHLY_WIDTH)
      .height(MONTHLY_HEIGHT)
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

    chart.addSeries("monthly", days, values)

    // Trend line
    val (slope:Double, shift:Double) = StatsCalculator.bestFit(dots)
    def trendLine (x:Double) = slope*x + shift

    val (xStart, xEnd) = StatsCalculator.minMax(days)
    val series = chart.addSeries(
      "trend: " + slope,
      List(xStart, xEnd),
      List(trendLine(xStart), trendLine(xEnd))
    )
    series.setChartXYSeriesRenderStyle(Line)

    chart
  }

  def weeklyCigarettesChart(dots:List[(Double, Double)]): CategoryChart = {
      val (days: List[String], values: List[Double]) = dots.map {
        case (x, y) => ( {
          val date = LocalDate.ofEpochDay(x.toInt)
          date.getDayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault)
        }, y)
      }.unzip

    //TODO weird import issue - move out from the method
    //TODO use i18n
    val chart = new CategoryChartBuilder()
      .width(WEEKLY_WIDTH)
      .height(WEEKLY_HEIGHT)
      .title("Week results")
      .xAxisTitle("Days")
      .yAxisTitle("Cigarettes")
      .theme(org.knowm.xchart.style.Styler.ChartTheme.GGPlot2)
      .build()

    chart.getStyler.setLegendVisible(false)
    chart.getStyler.setPlotContentSize(1.0)
    chart.getStyler.setYAxisDecimalPattern("###.#")

    chart.addSeries("weekly", days, values)

    chart
  }

  def createTempFile: String = {
    Files.createTempFile("bot", "_" + System.nanoTime()).toAbsolutePath.toString
  }

}
