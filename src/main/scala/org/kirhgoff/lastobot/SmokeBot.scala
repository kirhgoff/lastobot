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
  final case class Smoke(count:Int) extends BotAction
  final case class ShowSmokingStats() extends BotAction
  final case class Weight(count:Option[String]) extends BotAction
  final case class ShowWeightStats() extends BotAction
  final case class Reset() extends BotAction
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
case object ConfirmingSmoke extends State
case object GettingWeight extends State
case object ConfirmingWeight extends State
case object ChangingLocale extends State

//data
sealed trait Data
case object Empty extends Data
case object Yes extends Data
case object No extends Data
final case class UserSaid(text:String) extends Data
final case class UserSmoked(count:Int) extends Data
final case class UserMeasuredWeight(weight:Double) extends Data
final case class UserChangedLocale(locale:BotLocale) extends Data


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
    case Event(BotAction.ChangeLocale, _) => goto(ChangingLocale)

    //smokes
    case Event(BotAction.Smoke(count), _) => goto(ConfirmingSmoke) using UserSmoked(count)
    case Event(BotAction.ShowSmokingStats, _) => {
      logger.info("Showing smoking stats")
      userStorage.aggregatedByDateBefore(LocalDate.now.minusDays(30)) match {
        case data: List[(Long, Double)] if data.nonEmpty => {

          sender() ! Picture(senderId, ChartsBuilder.monthlyFile(data))
          sender() ! Picture(senderId, ChartsBuilder.weeklyFile(data.takeRight(7)))
        }
        case x => {
          logger.info (s"Got this:$x")
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

    //blah-blah
    case Event(UserSaid(text), _) =>
      //TODO master, use your commands, you can measure your stuff
      goto(Serving) using Empty
  }

  when(ConfirmingSmoke) {
    case Event(UserSaid(text), _) if Recognizer.yes(text) =>
      goto(Serving) using Yes
    case Event(UserSaid(text), _) if Recognizer.no(text) =>
      goto(Serving) using No
    case _ =>
      sender() ! Text(senderId, what)
      goto(Serving) using Empty
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

  when(ConfirmingWeight) {
    case Event(UserSaid(text), UserMeasuredWeight(value)) if Recognizer.yes(text) =>
      goto(Serving) using Yes
    case Event(UserSaid(text), _) if Recognizer.no(text) =>
      goto(Serving) using No
    case _ =>
      sender() ! Text(senderId, what)
      goto(Serving) using Empty
  }

  when(ChangingLocale) {
    case Event(UserSaid(text), _) if Recognizer.english(text) =>
      goto(Serving) using UserChangedLocale(English)
    case Event(UserSaid(text), _) if Recognizer.russian(text) =>
      goto(Serving) using UserChangedLocale(Russian)
    case _ =>
      sender() ! Text(senderId, what)
      goto(Serving) using Empty
  }

  onTransition {
    //------------------------------ Smoking ------------------------------
    case Serving -> ConfirmingSmoke => nextStateData match {
      case UserSmoked(count) => sender() ! Keyboard(senderId,
        youSmokeQuestion(count),
        Array(yesNo(locale)))
      case _ =>
    }
    case ConfirmingSmoke -> Serving => nextStateData match {
      case Yes => stateData match {
        case UserSmoked(count) => {
          userStorage.smoked(count)
          sender() ! Text(senderId, youSmokeConfirmed(count))
        }
        case _ => sender() ! Text(senderId, what) //TODO match any incoming to Serving using What
      }
      case _ => sender() ! Text(senderId, cancelled)
    }


    //------------------------------ Locale ------------------------------
    case Serving -> ChangingLocale =>
        //TODO move to transition
        sender() ! Keyboard(senderId, changeLocale, Array(englishRussian))

    case ChangingLocale -> Serving => nextStateData match {
      case UserChangedLocale(newLocale) =>
        locale = userStorage.updateLocale(newLocale)
      case other => logger.error(s"wrong state ChangingLocale -> Serving: $other")
    }
  }

  whenUnhandled {
    case Event(Reset, _) =>
      logger.info("Resetting state")
      locale = userStorage.getLocaleOr(English)
      goto(Serving) using Empty
  }

  initialize()
}
