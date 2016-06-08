package org.kirhgoff.lastobot

import java.util.Properties
import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeMessage}

import scala.io.Source

/**
  * Created by kirilllastovirya on 8/06/2016.
  */
object Mailer {
  val host = "smtp.gmail.com"
  val port = "587"

  val address = "lastobot@gmail.com"
  val username = "lastobot"
  val password = Source.fromFile(System.getProperty("user.home")
    + "/.lastobot/.mail").getLines.mkString

  def sendMail(text:String, subject:String) = {
    val properties = new Properties()
    properties.put("mail.smtp.port", port)
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")

    val session = Session.getDefaultInstance(properties, null)
    val message = new MimeMessage(session)
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
    message.setSubject(subject)
    message.setContent(text, "text/html")

    val transport = session.getTransport("smtp")
    transport.connect(host, username, password)
    transport.sendMessage(message, message.getAllRecipients)
  }

  def main(args:Array[String]) = {
    sendMail("aaaa", "bbb")
  }
}
