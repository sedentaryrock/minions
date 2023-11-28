package org.example

import org.example.model.Message

import scala.concurrent.Future

trait Publisher {
  def publish(messageId:String, topic: String): Future[Option[Message]]
}
