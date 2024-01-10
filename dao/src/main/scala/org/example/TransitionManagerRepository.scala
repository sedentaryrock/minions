package org.example

import org.example.model.Message
import org.reactivestreams.Publisher

trait TransitionManagerRepository {
  def pickUp(topic: String, status: String): Publisher[Message]
  def putDown(_id: String, status: String): Publisher[Message]

  def transition(topic: String, statusFrom: String, statusTo: String): Publisher[Message]
}
