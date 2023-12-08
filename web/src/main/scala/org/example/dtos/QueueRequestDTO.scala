package org.example.dtos

import org.example.model.Message

object QueueRequestDTO {
  def fromMessage(message: Message): QueueRequestDTO = {
    new QueueRequestDTO(message.messageId, message.topic, message.status)
  }
}

case class QueueRequestDTO(messageId: String, topic: String, status: String)
