package org.example.helpers

import com.mongodb.client.model.ReturnDocument
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.example.Configuration
import org.example.model.Message
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.FindOneAndUpdateOptions

object MongoCommon {
  val COLLECTION_NAME = "messages"

  val INSERT_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(true)
    .returnDocument(ReturnDocument.AFTER)

  val UPDATE_OPTIONS: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(false)
    .returnDocument(ReturnDocument.AFTER)

  val MESSAGE_CODEC_PROVIDER: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()

  val CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(MESSAGE_CODEC_PROVIDER)
    , DEFAULT_CODEC_REGISTRY
  )

  val MESSAGE_COLLECTION =
    MongoClients.create(Configuration.mongodbUrl)
      .getDatabase(Configuration.mongodbDbName)
      .getCollection(COLLECTION_NAME, classOf[Message])
      .withCodecRegistry(CODEC_REGISTRY)

}
