package org.example

import com.mongodb.client.model.ReturnDocument
import org.example.helpers.MongoCommon
import org.example.model.Message
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class JanitorRepositoryUsingMongo extends JanitorRepository {
  private val UPDATE_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(false)
    .returnDocument(ReturnDocument.AFTER)

  override def fetchHungMessages(cutoffTime: Instant): Publisher[Message] = {
    val search = Filters.and(
      Filters.exists("PICKED", exists = true),
      Filters.lt("pickedAt", cutoffTime)
    )

    MongoCommon.MESSAGE_COLLECTION.find(search)
  }

  override def markMessageAsDead(_id: String): Publisher[Message] = {
    val search = Filters.eq("_id", new ObjectId(_id))

    val updates = Updates.combine(
      Updates.set("status", "DEAD"),
      Updates.unset("PICKED")
    )

    MongoCommon.MESSAGE_COLLECTION.findOneAndUpdate(search, updates, UPDATE_OPTIONS)
  }
}

