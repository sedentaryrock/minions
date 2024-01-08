package org.example

import com.mongodb.client.model.ReturnDocument
import org.example.helpers.MongoCommon
import org.example.model.Message
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class TransitionManagerRepositoryUsingMongo extends TransitionManagerRepository {
  private val UPDATE_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(false)
    .returnDocument(ReturnDocument.AFTER)

  override def transition(topic: String, statusFrom: String, statusTo: String): Publisher[Message] = {
    val search = Filters.and(
      Filters.eq("topic", topic)
      , Filters.eq("status", statusFrom)
    )

    val updates = Updates.combine(
      Updates.set("status", statusTo)
      , Updates.set("updatedOn", Instant.now)
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, updates, UPDATE_OPTIONS)
  }
}
