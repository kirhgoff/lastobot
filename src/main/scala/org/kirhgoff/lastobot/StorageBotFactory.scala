package org.kirhgoff.lastobot

import scala.language.implicitConversions

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.util.Date

import com.typesafe.scalalogging.LazyLogging

// Imports core, which grabs everything including Query DSL
import com.mongodb.casbah.Imports._

/**
  * Created by kirilllastovirya on 1/05/2016.
  */

class StorageBotFactory(val databaseHost:String, val databasePort:Int) {
  val mongoClient = MongoClient(databaseHost, databasePort)

  def userStorageFor(senderId:Int) = new UserStorage(mongoClient(s"sender_$senderId"))
  def close() = mongoClient.close()
}

object DateConversions {
  implicit def localDateToUtilDate (date: java.util.Date) : LocalDate = {
    val instant = Instant.ofEpochMilli(date.getTime)
    LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate
  }

  implicit def localDateToUtilDate (localDate: LocalDate) : java.util.Date = java.sql.Date.valueOf(localDate)
}

//TODO Implement actor to perform db actions
class UserStorage(val db:MongoDB) extends LazyLogging {

  import DateConversions._

  // Locale collection
  def updateLocale(newLocale: BotLocale): BotLocale = {
    db("user_preferences").update(
      MongoDBObject("_id"->"locale"),
      MongoDBObject("value"->newLocale.toString),
      upsert = true
    )
    getLocaleOr(newLocale)
  }

  def getLocaleOr(defaultLocale: BotLocale): BotLocale =  {
    db("user_preferences").findOneByID("locale") match {
      case Some(pref) => {
        val localeString: String = pref.get("value").asInstanceOf[String]
        BotLocale(localeString)
      }
      case None => {
        logger.error(s"Found no locale, falling back: $defaultLocale")
        defaultLocale
      }
    }
  }

  val weights = db("weights")
  def weightMeasured(weight:Double):Unit = weightMeasured(weight, new Date)
  def weightMeasured(weight:Double, date:java.util.Date) = {
    weights.insert(MongoDBObject("weight" -> weight, "date" -> date, "epochDay" -> absoluteDays(date)))
  }

  def lastWeight():Option[Double] = {
    weights.find().sort(MongoDBObject("date" -> 1)).limit(1).toList.headOption match {
      case Some(result) => Some(result("weight").asInstanceOf[Double])
      case None => None
    }
  }

  // Smokes collection
  val smokes = db("smokes")

  def smoked(count:Int):Unit = smoked(count, new Date)
  def smoked(count:Int, date:java.util.Date) = {
    smokes.insert(MongoDBObject("count" -> count, "date" -> date, "epochDay" -> absoluteDays(date)))
  }

  def smokedOverall():Int = {
    val total = smokes.aggregate(List(MongoDBObject("$group" ->
      MongoDBObject("_id" -> null,
        "total" -> MongoDBObject("$sum" -> "$count")
      )
    )))
    total.results.headOption match {
      case None => 0
      case Some(result) => result("total").asInstanceOf[Int]
    }
  }

  def aggregatedByDateBefore(localDate: LocalDate):List [(Long, Double)] = {
    val total = smokes.aggregate(List(
      MongoDBObject("$match" ->
        MongoDBObject("epochDay" -> MongoDBObject("$gte" -> localDate.toEpochDay))
      ),
      MongoDBObject("$group" ->
        MongoDBObject(
          "_id" -> "$epochDay",
          "total" -> MongoDBObject("$sum" -> "$count")
        )
      ),
      MongoDBObject("$sort" ->
        MongoDBObject("_id" -> -1)
      )
    ))
    val seq = for (result <- total.results) yield (
      result("_id").asInstanceOf[Long],
      result("total") match {
        //TODO what to do with this?
        case i:Integer => i.doubleValue()
      }
    )
    seq.toList
  }

  def clearSmokes() = smokes.drop()

  def absoluteDays (date:LocalDate):Long = date.toEpochDay
}


object MongoTest {
  import DateConversions._

  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("sender_3")
    val userStorage = new UserStorage(db)
    val results = userStorage.aggregatedByDateBefore(LocalDate.of(2016, 5, 18))
    println(results)
  }
}