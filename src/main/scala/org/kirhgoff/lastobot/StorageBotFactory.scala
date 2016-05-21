package org.kirhgoff.lastobot

import java.time.LocalDate
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

//TODO Implement actor to perform db actions
class UserStorage(val db:MongoDB) extends LazyLogging {

  // Locale collection
  def updateLocale(newLocale: BotLocale): BotLocale = {
    val collection = db("user_preferences")
    //collection.insert(MongoDBObject("_id"->"locale", "value" -> newLocale.toString))
    collection.update(
      MongoDBObject("_id"->"locale"),
      MongoDBObject("value"->newLocale.toString),
      upsert = true
    )
    getLocaleOr(newLocale)
  }

  def getLocaleOr(defaultLocale: BotLocale): BotLocale =  {
    val collection = db("user_preferences")
    collection.findOneByID("locale") match {
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

  // Smokes collection
  val smokes = db("smokes")

  def smoked(count:Int) = smoked(count, new Date)
  def smoked(count:Int, date:LocalDate) = smoked(count, java.sql.Date.valueOf(date))

  def smoked(count:Int, date:Date) = {
    smokes.insert(MongoDBObject("count" -> count, "date" -> date))
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

  //db.smokes.aggregate({$group: {_id: {$dateToString: {format:"%Y-%d-%m", date:"$date"}}, total:{$sum:"$count"}}})
  def getRawData():Map[LocalDate, Int] = {
//    db.smokes.aggregate({
//      $match: {
//        _id : {
//        $gte : {
//        $subtruct : [
//        $dayOfYear: new Date(),
//        30
//        ]
//      }
//      }
//      },
//      $group: {
//        _id: "$date"
//        total:{
//        $sum:"$count"
//      }
//      }
//    })
//
//    db.smokes.aggregate({
//      $group: {
//        _id: "$date",
//        total:{
//        $sum:"$count"
//      }
//      }
//    })
  }

  def clearSmokes() = smokes.drop()
}


object MongoTest {
  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("sender_3")
    val userStorage = new UserStorage(db)
    var locale = userStorage.getLocaleOr(English)
    println("1:" + locale)

    locale = userStorage.updateLocale(Russian)
    println("2:" + locale)

    locale = userStorage.updateLocale(English)
    println("3:" + locale)
  }

  def main2(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("sender_3")
    val collection = db("user_preferences")
    //collection.insert(MongoDBObject("_id"->"locale", "value" -> "english"))
    val locale = collection.findOneByID("locale").get("value")
    println(locale)
  }
}