package org.example

import org.example.helpers.MongoCommon
import org.example.helpers.MongoCommon.UPDATE_OPTIONS
import org.example.model.Message
import org.mongodb.scala.model.{Filters, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class TransitionManagerRepositoryUsingMongo extends TransitionManagerRepository {

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
