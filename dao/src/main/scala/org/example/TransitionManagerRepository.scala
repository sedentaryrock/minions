package org.example

import org.example.model.Message
import org.reactivestreams.Publisher

trait TransitionManagerRepository {
  def transition(topic: String, statusFrom: String, statusTo: String): Publisher[Message]
}
