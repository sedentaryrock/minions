package org.example

import org.example.model.Message

import scala.concurrent.Future

trait QueueManager {
  def queue(messageId:String, topic: String, status: String): Future[Message]
}
