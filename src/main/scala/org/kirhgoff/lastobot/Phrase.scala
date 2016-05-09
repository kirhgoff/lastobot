package org.kirhgoff.lastobot

import java.util.Random

/**
  * Created by kirilllastovirya on 5/05/2016.
  */

trait BotLocale
case object Russian extends BotLocale
case object English extends BotLocale

object Recognizer {
  def yes (text:String) = text.startsWith("да") || text.startsWith("yes")
  def no(text:String) = text.startsWith("нет") || text.startsWith("no")
}

//TODO refactor locale match to partial function

object Phrase {
  val random = new Random

  def anyOf(text:String*) = text(random.nextInt(text.length))

  def intro(locale: BotLocale) = locale match {
    case English =>
      "This is prototype of SmokeBot [v1.1]\n" +
        "The idea of the bot is that when you need to control something in you life " +
        "you can choose a way to measure it and make the bot take care of measurements " +
        "you just provide the numbers and bot will be able to give you statistics. " +
        "As a first attempt we take a smoking habit. Every time you smoke (or when you " +
        "notice a bunch of stubs in your ashtray) you let bot know the number with command " +
        "/smoke (you could specify the amount). When you want to see how many cigarettes " +
        "you smoke, you ask for /stats and bot gives you some stats. So... how may I serve " +
        "you, Master?"
    case Russian =>
      "Это прототип бота SmokeBot [v1.1]\n" +
        "Идея состоит в том, что если вам хочется контроллировать что-то в вашей " +
        "жизни, один из способов - это выбрать способ мерять это, и с помощью бота наблюдать " +
        "за этим измерением, будь то ваш вес или количество выкуренных вами сигарет. Вы можете " +
        "сообщить боту сколько вы выкурили сигарет недавно /smoke, а он будет готов выдать вам " +
        "статистику. Так что... чем я могу служить вам, Хозяин?"
  }

  def obey(locale: BotLocale): String = locale match {
    case English => anyOf("Yes, my master!", "I am listening, my master!")
    case Russian => anyOf("Да, хозяин!", "Да, мой господин!", "Слушаю и повинуюсь!")
  }

  def whatFoodToServe(locale: BotLocale): String = locale match {
    case English => anyOf("What food may I serve you, my master?", "What would you like, master?")
    case Russian => anyOf("Чего изволити?", "Чтобы вы хотели?")
  }

  def foodChoices(locale: BotLocale): Array[String] = locale match {
    case English => Array("bread", "butter", "beer")
    case Russian => Array("хлеб", "масло", "пиво")
  }

  def sayYes(locale: BotLocale): String = locale match {
    case English => "Say \"yes\""
    case Russian => "Скажи \"да\""
  }

  def yesNo(locale: BotLocale): Array[String] = locale match {
    case Russian => Array("да", "нет")
    case English => Array("yes", "no")
  }

  def abuseReply(locale: BotLocale): String = locale match {
    case Russian => "Манда!"
    case English => "ABKHSS"
  }

  def what(locale: BotLocale): String = locale match {
    case English => "You got me confused"
    case Russian => "Ничего не понял"
  }

  def cancelled(locale: BotLocale): String = locale match {
    case English => "OK, cancelled."
    case Russian => "Отменяю"
  }

  def youSmokeQuestion(count: Int, locale: BotLocale): String = locale match {
    case Russian => s"Сигарет выкурено: $count?" //TODO different cases
    case English => s"You smoked $count cigarettes?"
  }

  def youSmokeConfirmed(count: Int, locale: BotLocale): String = locale match {
    case English => s"Done, you smoked $count cigarettes, master"
    case Russian => s"Хозяин, сигарет выкурено: $count"
  }

  def smokedOverall(smoked: Int, locale: BotLocale): String = locale match {
    case English => s"Master, you smoke $smoked cigarettes overall"
    case Russian => s"Хозяин, вы выкурили всего $smoked сигарет"
  }
}
