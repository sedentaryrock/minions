package org.example

import com.mongodb.client.model.ReturnDocument
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.example.model.Message
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.reactivestreams.Publisher

import java.time.Instant

class MessageRepositoryUsingMongo extends MessageRepository {
  private val COLLECTION_NAME = "messages"

  private val INSERT_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(true)
    .returnDocument(ReturnDocument.AFTER)

  private val MESSAGE_CODEC_PROVIDER: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()

  private val CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(MESSAGE_CODEC_PROVIDER)
    , DEFAULT_CODEC_REGISTRY
  )

  private val MESSAGE_COLLECTION =
    MongoClients.create(Configuration.mongodbUrl)
      .getDatabase(Configuration.mongodbDbName)
      .getCollection(COLLECTION_NAME, classOf[Message])
      .withCodecRegistry(CODEC_REGISTRY)

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

    MESSAGE_COLLECTION.findOneAndUpdate(search, message, INSERT_OPTIONS)
  }
}