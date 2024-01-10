package org.example
import com.mongodb.client.model.ReturnDocument
import org.example.helpers.MongoCommon
import org.example.model.Message
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class MessageRepositoryUsingMongo extends MessageRepository {
  private val UPDATE_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(false)
    .returnDocument(ReturnDocument.AFTER)

  override def updateOutput(_id: String, output: Object): Publisher[Message] = {
    val search = Filters.eq("_id", new ObjectId(_id))

    val updates = Updates.combine(
      Updates.set("output", output)
      , Updates.set("outputUpdatedOn", Instant.now)
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, updates, UPDATE_OPTIONS)
  }
}
