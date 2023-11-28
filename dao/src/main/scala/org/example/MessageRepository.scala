package org.example

import org.example.model.Message

trait MessageRepository {
  def insert(message: Message): Message
}