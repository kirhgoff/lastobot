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

  def phraseCase(text:String, vars:Any*)
                (caseLocale: BotLocale)
                (implicit locale: BotLocale) :PartialFunction[BotLocale, String] = {
    case locale if locale == caseLocale => text.format(vars.map(_.asInstanceOf[AnyRef]): _*)
  }

  def russian (text:String, vars:Any*)(implicit locale: BotLocale) = phraseCase(text, vars:_*)(Russian)
  def english (text:String, vars:Any*)(implicit locale: BotLocale) = phraseCase(text, vars:_*)(English)
  /*
  def russian(text:String, vars:Any*)(implicit locale: BotLocale)
    :PartialFunction[BotLocale, String] =
    {case locale if locale == Russian => String.format(text, vars.map(_.asInstanceOf[AnyRef]): _*)}

  def english(text:String, vars:Any*)(implicit locale: BotLocale)
    :PartialFunction[BotLocale, String] =
    {case locale if locale == English => String.format(text, vars.map(_.asInstanceOf[AnyRef]): _*)}
  */

  def russianArray(text:String*)(implicit locale: BotLocale)
    :PartialFunction[BotLocale, Array[String]] =
    {case locale if locale == Russian => text.toArray}

  def englishArray(text:String*)(implicit locale: BotLocale)
    :PartialFunction[BotLocale, Array[String]] =
    //{case locale if locale == English => text.asInstanceOf[Array[String]]}
    {case locale if locale == English => text.toArray}


  //TODO make implicits
  def compose(
     partial1:PartialFunction[BotLocale, String],
     partial2:PartialFunction[BotLocale, String]
   )(implicit locale: BotLocale) =
    partial1.orElse(partial2).apply(locale)

  def composeArray(
     partial1:PartialFunction[BotLocale, Array[String]],
     partial2:PartialFunction[BotLocale, Array[String]]
   )(implicit locale: BotLocale) =
    partial1.orElse(partial2).apply(locale)

  def intro(implicit locale: BotLocale) = compose(
    english(
      "This is prototype of SmokeBot [v1.1]\n" +
        "The idea of the bot is that when you need to control something in you life " +
        "you can choose a way to measure it and make the bot take care of measurements " +
        "you just provide the numbers and bot will be able to give you statistics. " +
        "As a first attempt we take a smoking habit. Every time you smoke (or when you " +
        "notice a bunch of stubs in your ashtray) you let bot know the number with command " +
        "/smoke (you could specify the amount). When you want to see how many cigarettes " +
        "you smoke, you ask for /stats and bot gives you some stats. So... how may I serve " +
        "you, Master?"),
    russian(
      "Это прототип бота SmokeBot [v1.1]\n" +
        "Идея состоит в том, что если вам хочется контроллировать что-то в вашей " +
        "жизни, один из способов - это выбрать способ мерять это, и с помощью бота наблюдать " +
        "за этим измерением, будь то ваш вес или количество выкуренных вами сигарет. Вы можете " +
        "сообщить боту сколько вы выкурили сигарет недавно /smoke, а он будет готов выдать вам " +
        "статистику. Так что... чем я могу служить вам, Хозяин?")
  )

  def obey(implicit locale: BotLocale): String = compose (
    english(anyOf("Yes, my master!", "I am listening, my master!")),
    russian(anyOf("Да, хозяин!", "Да, мой господин!", "Слушаю и повинуюсь!"))
  )

  def whatFoodToServe(implicit locale: BotLocale): String = compose (
    english(anyOf("What food may I serve you, my master?", "What would you like, master?")),
    russian(anyOf("Чего изволити?", "Чтобы вы хотели?"))
  )

  def foodChoices(implicit locale: BotLocale): Array[String] = composeArray (
    englishArray("bread", "butter", "beer"),
    russianArray("хлеб", "масло", "пиво")
  )

  def sayYes(implicit locale: BotLocale): String = compose (
    english("Say \"yes\""),
    russian("Скажи \"да\"")
  )

  def yesNo(implicit locale: BotLocale): Array[String] = composeArray (
    russianArray("да", "нет"),
    englishArray("yes", "no")
  )

  def abuseReply(implicit locale: BotLocale): String = compose (
    russian("Манда!"),
    english("ABKHSS")
  )

  def what(implicit locale: BotLocale): String = compose (
    english("You got me confused"),
    russian("Ничего не понял")
  )

  def cancelled(implicit locale: BotLocale): String = compose (
    english("OK, cancelled."),
    russian("Отменяю")
  )

  def youSmokeQuestion(count: Int)(implicit locale: BotLocale): String = compose (
    russian(s"Сигарет выкурено: $count?"), //TODO different cases
    english(s"You smoked $count cigarettes?")
  )

  def youSmokeConfirmed(count: Int)(implicit locale: BotLocale): String = compose (
    english(s"Done, you smoked $count cigarettes, master"),
    russian(s"Хозяин, сигарет выкурено: $count")
 )

  def smokedOverall(smoked: Int)(implicit locale: BotLocale): String = compose (
    english(s"Master, you smoke $smoked cigarettes overall"),
    russian(s"Хозяин, вы выкурили всего $smoked сигарет")
  )
}
