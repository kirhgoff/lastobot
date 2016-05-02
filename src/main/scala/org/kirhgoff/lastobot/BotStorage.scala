package org.kirhgoff.lastobot

import java.time.{LocalDate, ZoneId}
import java.util.{Date, Observer}

// Imports core, which grabs everything including Query DSL
import com.mongodb.casbah.Imports._

/**
  * Created by kirilllastovirya on 1/05/2016.
  */
//class BotStorage {
//  val databaseUrl = "mongodb://localhost" //Use configuration
//  val mongoClient = MongoClient(databaseUrl)
//  val SmokingCollection = "smoking"
//
//  def smokeAdd(sender:Int) = {
//    //TODO use GMT and save user timezone additionally
//    val database = databaseFor(sender)
//    val collection = database.getCollection(SmokingCollection)
//
//    collection.insertOne(Document("count" -> 1, "date" -> new Date()))
//  }
//
//  def smokeGet(sender:Int) = {
//    val database = databaseFor(sender)
//    val collection = database.getCollection(SmokingCollection)
//    val aggregate:Seq[Bson] = List(group("_id", sum("totalQuantity", "$count")))
//    val result = collection.aggregate (aggregate).subscribe()
//    println(result.)
//  }
//
//  def databaseFor(sender: Int): MongoDatabase = {
//    mongoClient.getDatabase("sender_" + sender)
//  }
//}



object Test {
  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)
    val db = mongoClient("test")
    val collection = db("smokes")
    val totals = collection.aggregate(List(MongoDBObject("$group" ->
      MongoDBObject("_id" -> "null",
        "total" -> MongoDBObject("$sum" -> "$count")
      )
    )))
    println(totals.results)
  }
}