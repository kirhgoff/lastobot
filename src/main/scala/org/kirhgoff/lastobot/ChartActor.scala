package org.kirhgoff.lastobot

import java.time.LocalDate
import java.time.format.TextStyle
import java.util
import java.util.Locale

import com.mongodb.casbah.Imports._
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.style.Styler.ChartTheme
import org.knowm.xchart.style.Theme
import org.knowm.xchart.{BitmapEncoder, CategoryChartBuilder, CategorySeries}


/**
  * Created by kirilllastovirya on 19/05/2016.
  */
class ChartActor {

}

object ChartTest extends {
  //http://knowm.org/open-source/xchart/xchart-example-code/
  def main(args: Array[String]) {
    import scala.collection.JavaConverters._

    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("sender_3")
    val userStorage = new UserStorage(db)
    val results = userStorage.aggregatedByDateBefore(LocalDate.of(2016, 5, 14))

    val (days:List[String], values:List[Double]) = results.map {
      case (x, y) => (LocalDate.ofEpochDay(x).getDayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault), y)
    }.unzip

    // Create Chart
    val chart = new CategoryChartBuilder()
      .width(400)
      .height(600)
      .title("Week results")
      .xAxisTitle("Days")
      .yAxisTitle("Cigarettes")
      .theme(ChartTheme.GGPlot2)
      .build()

    // Customize Chart
    chart.getStyler.setLegendVisible(false)
    chart.getStyler.setPlotContentSize(1.0)
    //chart.getStyler.setAxisTicksVisible(false)
    //chart.getStyler.setPlotTicksMarksVisible(true)
    chart.getStyler.setYAxisDecimalPattern("###.#")


    // Series
    val daysJava = days.asJava
    val valuesJava:util.List[Number] = values.map (x => x.asInstanceOf[Number]).asJava
    val series: CategorySeries = chart.addSeries("cigarettes", daysJava, valuesJava)
    
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
