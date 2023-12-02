package org.example

import com.mongodb.client.model.ReturnDocument
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.example.model.Message
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Publisher, Subscriber}

class MessageRepositoryUsingMongo extends MessageRepository {
  private val collectionName = "messages"
  private val INSERT_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(true)
    .returnDocument(ReturnDocument.AFTER)

  implicit private val codecProvider: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()
  implicit private val codecRegistry: CodecRegistry = fromRegistries(fromProviders(codecProvider), DEFAULT_CODEC_REGISTRY)
  private val mongoClient = MongoClients.create(Configuration.mongodbUrl)
  private val db = mongoClient.getDatabase(Configuration.mongodbDbName)
  private val messagesCollection = db.getCollection(collectionName, classOf[Message]).withCodecRegistry(codecRegistry)

  override def queue(messageId: String, topic: String, status: String): Publisher[Message] = {
    val search = Filters.and(
      Filters.eq("message_id", messageId)
      , Filters.eq("topic", topic)
    )

    val message = Updates.combine(
      Updates.setOnInsert("message_id", messageId)
      , Updates.setOnInsert("topic", topic)
      , Updates.setOnInsert("status", status)
      , Updates.inc("seen", 1)
    )

    messagesCollection.findOneAndUpdate(search, message, INSERT_OPTIONS)
  }
}