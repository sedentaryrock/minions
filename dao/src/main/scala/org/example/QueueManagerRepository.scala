package org.example

import org.example.model.Message
import org.reactivestreams.Publisher

trait QueueManagerRepository {
  def queue(messageId: String, topic: String, status: String): Publisher[Message]
}