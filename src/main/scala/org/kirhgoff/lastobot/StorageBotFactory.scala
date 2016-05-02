package org.kirhgoff.lastobot

import java.time.{LocalDate, ZoneId}
import java.util.{Date, Observer}

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
class UserStorage(val db:MongoDB) {
  val smokes = db("smokes")
  def smoked(count:Int) = {
    smokes.insert(MongoDBObject("count" -> count, "date" -> new Date))
  }

  def smokedOverall():Int = {
    val total = smokes.aggregate(List(MongoDBObject("$group" ->
      MongoDBObject("_id" -> null,
        "total" -> MongoDBObject("$sum" -> "$count")
      )
    )))
    total.results.headOption.getOrElse("total", 1).asInstanceOf[Int]
  }

  def clear() = smokes.drop()
}


object Test {
  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("sender_3")
    val collection = db("smokes")
    for (i <- 1 to 10) collection.insert(MongoDBObject("count"->1, "date" -> new Date))
    val totals = collection.aggregate(List(MongoDBObject("$group" ->
      MongoDBObject("_id" -> null,
        "total" -> MongoDBObject("$sum" -> "$count")
      )
    )))
    println(totals.results)
    println(totals.results.head("total"))
  }
}