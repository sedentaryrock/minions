package org.example

import com.mongodb.client.model.ReturnDocument
import org.example.helpers.MongoCommon
import org.example.model.Message
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class TransitionManagerRepositoryUsingMongo extends TransitionManagerRepository {
  private val UPDATE_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(false)
    .returnDocument(ReturnDocument.AFTER)

  override def pickUp(topic: String, status: String): Publisher[Message] = {
    val search = Filters.and(
      Filters.eq("topic", topic)
      , Filters.eq("status", status)
      , Filters.exists("WORKED_UPON", exists = false)
    )

    val updates = Updates.combine(
      Updates.set("WORKED_UPON", true)
      , Updates.set("pickedOn", Instant.now)
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, updates, UPDATE_OPTIONS)
  }

  override def putDown(_id: String, status: String): Publisher[Message] = {
    val search = Filters.eq("_id", new ObjectId(_id))

    val updates = Updates.combine(
      Updates.set("status", status)
      , Updates.unset("WORKED_UPON")
      , Updates.set("putOn", Instant.now)
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, updates, UPDATE_OPTIONS)
  }
}
