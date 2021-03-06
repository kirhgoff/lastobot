package org.kirhgoff.lastobot

import java.time.LocalDate
import java.util.Objects

import akka.actor.{FSM, LoggingFSM}
import com.typesafe.scalalogging.LazyLogging
import info.mukel.telegram.bots.api.Message
import org.kirhgoff.lastobot
import org.kirhgoff.lastobot.BotAction.Reset
import org.kirhgoff.lastobot.Phrase._

import scala.util.{Failure, Success, Try}

//What Telegram bot receives
trait UserMessages
case class UserCommand(sender:Int, commandName:String, args:Seq[String]) extends UserMessages
case class UserTextMessage(msg:Message) extends UserMessages

//What commands bot processes
trait BotAction
object BotAction {
  final case class Start() extends BotAction
  final case class ChangeLocale() extends BotAction
  final case class Smoke(count:Option[String]) extends BotAction
  final case class ShowSmokingStats() extends BotAction
  final case class Weight(weight:Option[String]) extends BotAction
  final case class ShowWeightStats() extends BotAction
  final case class Reset() extends BotAction
  final case class Bug() extends BotAction
  final case class Feature() extends BotAction
}

//What it sends back
final case class Text(sender:Int, text:String)
final case class Keyboard(sender:Int, text:String, buttons:Array[Array[String]]) {
  override def equals(other: Any) = other match {
    case Keyboard (s, t, b) if s == sender &&
      ((t == null && text == null) || t.equals(text)) &&
      Objects.deepEquals(b, buttons) => true
    case _ => false
  }
  override def toString =
    s"Keyboard for $sender text=($text) buttons=${buttons.deep.mkString}"

}
final case class Picture(sender:Int, filePath:String)

//states
sealed trait State
case object Serving extends State
case object GettingSmoked extends State
case object GettingWeight extends State
case object ConfirmingWeight extends State
case object GettingUserFeedback extends State
case object ChangingLocale extends State

//data
sealed trait Data
case object Empty extends Data
case object Yes extends Data
case object No extends Data
final case class UserSaid(text:String) extends Data
final case class UserWish(wishType:BugOrFeature, text:String) extends Data
final case class UserSmoked(count:Int) extends Data
final case class UserMeasuredWeight(weight:Double) extends Data
final case class UserChangedLocale(locale:BotLocale) extends Data

trait BugOrFeature
case object Bug extends BugOrFeature
case object Feature extends BugOrFeature

/**
  * Created by kirilllastovirya on 26/04/2016.
  */
class SmokeBot(val senderId: Int, val userStorage: UserStorage)
  extends LoggingFSM[State, Data] with LazyLogging {

  //By default bot is english
  implicit var locale:BotLocale = userStorage.getLocaleOr(English)

  startWith(Serving, Empty)

  when(Serving) {
    //start
    case Event(BotAction.Start, _) =>
      sender() ! Text(senderId, intro)
      //TODO show keyboard with trackers
      stay

    //setlocale
    case Event(BotAction.ChangeLocale, _) =>
      sender() ! Keyboard(senderId, changeLocale, Array(englishRussian))
      goto(ChangingLocale)

    //smokes
    case Event(BotAction.Smoke(stringOption), _) => stringOption match {
      case Some(stringValue) => Try {stringValue.toInt} match {
        case Success (value) =>
          logger.info(s"User provided valid value for smoke: $value")
          sender() ! Text(senderId, youSmoked(value))
          userStorage.smoked(value)
          stay
        case Failure(ex) =>
          //TODO extract method say
          //TODO add what with parameter - blah what?
          sender() ! Text(senderId, what)
          goto(Serving) using Empty
      }
      case None =>
        sender() ! Text(senderId, howManyCigarettes)
        goto(GettingSmoked)
    }

    case Event(BotAction.ShowSmokingStats, _) => {
      logger.info("Showing smoking stats")
      userStorage.smokesAggregatedByDateBefore(LocalDate.now.minusDays(30)) match {
        case data: List[(Long, Double)] if data.nonEmpty => {

          sender() ! Picture(senderId, ChartsBuilder.monthlyFile(Phrase.cigarettes, data))
          sender() ! Picture(senderId, ChartsBuilder.weeklyFile(Phrase.cigarettes, data.takeRight(7)))
        }
        case x => {
          logger.info (s"Trying to get smoking stats, got this:$x")
          sender() ! Text(senderId, noDataYet)
        }
      }
      stay
    }

    //weight
    case Event(BotAction.Weight(stringOption), _) => stringOption match {
      case Some(stringValue) => Try {stringValue.toDouble} match {
        case Success (value) =>
          logger.info(s"User provided valid value for weight: $value")
          sender() ! Text(senderId, weightMeasured(value))
          userStorage.weightMeasured(value)
          stay
        case Failure(ex) =>
          sender() ! Text(senderId, what) //TODO extract method say
          goto(Serving) using Empty
      }
      case None =>
        sender() ! Text(senderId, typeYourWeight)
        goto(GettingWeight)
    }
    case Event(BotAction.ShowWeightStats, _) => {
      logger.info("Showing weight stats")
      userStorage.weightAggregatedByDateBefore(LocalDate.now.minusDays(30)) match {
        case data: List[(Long, Double)] if data.nonEmpty => {

          sender() ! Picture(senderId, ChartsBuilder.monthlyFile(Phrase.weight, data))
          sender() ! Picture(senderId, ChartsBuilder.weeklyFile(Phrase.weight, data.takeRight(7)))
        }
        case x => {
          logger.info (s"Trying to get smoking stats, got this:$x")
          sender() ! Text(senderId, noDataYet)
        }
      }
      stay
    }
    case Event(BotAction.Bug, _) =>
      sender() ! Text(senderId, whenFinishedTypeSubmit)
      goto(GettingUserFeedback) using UserWish(Bug, "Bug: ")

    case Event(BotAction.Feature, _) =>
      sender() ! Text(senderId, whenFinishedTypeSubmit)
      goto(GettingUserFeedback) using UserWish(Feature, "Feature: ")

    //blah-blah
    case Event(UserSaid(text), _) =>
      //TODO master, use your commands, you can measure your stuff
      goto(Serving) using Empty
  }

  when(GettingSmoked) {
    case Event(UserSaid(text), _) => Try {text.toInt} match {
      case Success (value) =>
        //TODO joke about master getting too much weight
        logger.info(s"GettingSmoked: Parsed value for smokes: $value")
        userStorage.smoked(value)
        sender() ! Text(senderId, youSmoked(value)) //TODO print overall today
        goto (Serving) using Empty
      case Failure(ex) =>
        sender() ! Text(senderId, what) //TODO joke about master being ashamed
        goto(Serving) using Empty
    }
  }

  when(GettingWeight) {
    case Event(UserSaid(text), _) => Try {text.toDouble} match {
      case Success (value) =>
        //TODO joke about master getting too much weight
        logger.info(s"GettingWeight: Parsed value for weight: $value")
        userStorage.weightMeasured(value)
        sender() ! Text(senderId, weightMeasured(value))
        goto (Serving) using Empty
      case Failure(ex) =>
        sender() ! Text(senderId, what) //TODO joke about master being ashamed
        goto(Serving) using Empty
    }
  }

  when(ChangingLocale) {
    case Event(UserSaid(text), _) if Recognizer.english(text) =>
      userStorage.updateLocale(English)
      goto(Serving) using Empty
    case Event(UserSaid(text), _) if Recognizer.russian(text) =>
      userStorage.updateLocale(Russian)
      goto(Serving) using Empty
    case other =>
      logger.error(s"wrong state ChangingLocale -> Serving: $other")
      sender() ! Text(senderId, what)
      goto(Serving) using Empty
  }

  when(GettingUserFeedback) {
    case Event(UserSaid(text), UserWish(kind, body)) if Recognizer.finished(text) =>
      Mailer.sendMail(s"${kind.toString.toUpperCase}: feedback from $senderId", body)
      goto(Serving) using Empty
    case Event(UserSaid(text), UserWish(kind, body)) =>
      stay using UserWish(kind, body ++ "\n" ++ text)
  }

//  onTransition {
//  }

  whenUnhandled {
    case Event(Reset, _) =>
      logger.info("Resetting state")
      locale = userStorage.getLocaleOr(English)
      goto(Serving) using Empty
  }

  initialize()
}
