package org.example

import org.example.model.Message
import org.reactivestreams.Publisher

trait MessageRepository {
  def updateOutput(_id: String, output: Object): Publisher[Message]
}
