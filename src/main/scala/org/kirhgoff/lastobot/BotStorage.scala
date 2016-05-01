package org.kirhgoff.lastobot

import java.time.{LocalDate, ZoneId}
import java.util.{Date, Observer}

import org.bson.conversions.Bson
import org.mongodb.scala.{Completed, MongoClient, MongoDatabase}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Accumulators._

/**
  * Created by kirilllastovirya on 1/05/2016.
  */
class BotStorage {
  val databaseUrl = "mongodb://localhost" //Use configuration
  val mongoClient = MongoClient(databaseUrl)
  val SmokingCollection = "smoking"

  def smokeAdd(sender:Int) = {
    //TODO use GMT and save user timezone additionally
    val database = databaseFor(sender)
    val collection = database.getCollection(SmokingCollection)

    collection.insertOne(Document("count" -> 1, "date" -> new Date()))
  }

  def smokeGet(sender:Int) = {
    val database = databaseFor(sender)
    val collection = database.getCollection(SmokingCollection)
    val aggregate:Seq[Bson] = List(group("_id", sum("totalQuantity", "$count")))
    val result = collection.aggregate (aggregate).subscribe()
    println(result.)
  }

  def databaseFor(sender: Int): MongoDatabase = {
    mongoClient.getDatabase("sender_" + sender)
  }
}

object Test {
  def main(args: Array[String]) {
    val storage = new BotStorage
    for (i <- 1 to 10) storage.smokeAdd(1)
    storage.smokeGet(1)
  }
}