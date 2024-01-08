package org.example.helpers

import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.example.Configuration
import org.example.model.Message
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

object MongoCommon {
  private val COLLECTION_NAME = "messages"

  private val MESSAGE_CODEC_PROVIDER: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()

  private val CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(MESSAGE_CODEC_PROVIDER)
    , DEFAULT_CODEC_REGISTRY
  )

  val MESSAGE_COLLECTION: MongoCollection[Message] =
    MongoClients.create(Configuration.mongodbUrl)
      .getDatabase(Configuration.mongodbDbName)
      .getCollection(COLLECTION_NAME, classOf[Message])
      .withCodecRegistry(CODEC_REGISTRY)
}
