package org.example.model

import org.bson.types.ObjectId

import java.time.Instant

object Message {
  def apply(messageId: String, topic: String, status: String): Message = {
    apply(messageId, topic, status, None, None)
  }

  def apply(messageId: String, topic: String, status: String, body: Option[Object]): Message = {
    apply(messageId, topic, status, body, None)
  }

  def apply(messageId: String, topic: String, status: String, body: Option[Object], metadata: Option[Object]): Message =
    Message(new ObjectId(), messageId, topic, status, body, metadata, None, None, None, None)
}

case class Message(
                    _id: ObjectId,
                    messageId: String,
                    topic: String,
                    status: String,
                    body: Option[Object],
                    metadata: Option[Object],
                    output: Option[Object],
                    error: Option[Object],
                    createdOn: Option[Instant],
                    updatedOn: Option[Instant]
                  )
