package org.example.model

import org.bson.types.ObjectId

import java.time.Instant

object Message {
  def apply(messageId: String, topic: String, status: String): Message = {
    apply(messageId, topic, status, None, None)
  }

  def apply(messageId: String, topic: String, status: String, body: Option[Array[Byte]]): Message = {
    apply(messageId, topic, status, body, None)
  }

  def apply(messageId: String, topic: String, status: String, body: Option[Array[Byte]], metadata: Option[Array[Byte]]): Message =
    Message(new ObjectId(), messageId, topic, status, body, metadata, None, None, None, None)
}

case class Message(
                    _id: ObjectId,
                    messageId: String,
                    topic: String,
                    status: String,
                    body: Option[Array[Byte]],
                    metadata: Option[Array[Byte]],
                    output: Option[Array[Byte]],
                    error: Option[Array[Byte]],
                    createdOn: Option[Instant],
                    updatedOn: Option[Instant]
                  )
