package org.kirhgoff.lastobot

/**
  * Created by kirilllastovirya on 5/05/2016.
  */

trait BotLocale
case object Russian extends BotLocale
case object English extends BotLocale

object PhraseStorage {
  def start(locale: BotLocale) = {
    locale match {
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
        "Смысл бота состоит в том, что если вам хочется контроллировать что-то в вашей " +
        "жизни, один из способов - это выбрать способ мерять это, и с помощью бота наблюдать " +
        "за этим измерением, будь то ваш вес или количество выкуренных вами сигарет. Вы можете " +
        "сообщить боту сколько вы выкурили сигарет недавно /smoke, а он будет готов выдать вам " +
        "статистику. Так что... чем я могу служить вам, Хозяин?"
  }


}
