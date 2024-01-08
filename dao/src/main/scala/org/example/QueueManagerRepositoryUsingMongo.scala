package org.example

import com.mongodb.client.model.ReturnDocument
import org.example.helpers.MongoCommon
import org.example.model.Message
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class QueueManagerRepositoryUsingMongo extends QueueManagerRepository {
  private val INSERT_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(true)
    .returnDocument(ReturnDocument.AFTER)

  override def queue(messageId: String, topic: String, status: String): Publisher[Message] = {
    val search = Filters.and(
      Filters.eq("messageId", messageId)
      , Filters.eq("topic", topic)
    )

    val message = Updates.combine(
      Updates.setOnInsert("messageId", messageId)
      , Updates.setOnInsert("topic", topic)
      , Updates.setOnInsert("status", status)
      , Updates.setOnInsert("createdOn", Instant.now())
      , Updates.inc("seen", 1)
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, message, INSERT_OPTIONS)
  }
}