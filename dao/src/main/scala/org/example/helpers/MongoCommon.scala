package org.example.helpers

import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}
import org.example.Configuration
import org.example.model.{Message, TaskConfiguration}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

object MongoCommon {
  private val MESSAGE_COLLECTION_NAME = "messages"
  private val TASK_CONFIGURATION_COLLECTION_NAME = "task_configurations"

  private val MESSAGE_CODEC_PROVIDER: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()
  private val TASK_CONFIGURATION_CODEC_PROVIDER: CodecProvider = Macros.createCodecProviderIgnoreNone[TaskConfiguration]()

  private val MESSAGE_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(MESSAGE_CODEC_PROVIDER)
    , DEFAULT_CODEC_REGISTRY
  )

  private val TASK_CONFIGURATION_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(TASK_CONFIGURATION_CODEC_PROVIDER, new FiniteDurationCodecProvider)
    , DEFAULT_CODEC_REGISTRY
  )

  private def buildMongoUri(): String = {
    if (Configuration.mongodbUser.nonEmpty && Configuration.mongodbPassword.nonEmpty) {
      val baseUrl = Configuration.mongodbUrl
      val userPass = s"${Configuration.mongodbUser}:${Configuration.mongodbPassword}@"
      val authDb = Configuration.mongodbAuthDb
      baseUrl.replaceFirst("mongodb://", s"mongodb://$userPass") + s"/?authSource=$authDb"
    } else {
      Configuration.mongodbUrl
    }
  }

  val MESSAGE_COLLECTION: MongoCollection[Message] =
    MongoClients.create(buildMongoUri())
      .getDatabase(Configuration.mongodbDbName)
      .getCollection(MESSAGE_COLLECTION_NAME, classOf[Message])
      .withCodecRegistry(MESSAGE_CODEC_REGISTRY)

  val TASK_CONFIGURATION_COLLECTION: MongoCollection[TaskConfiguration] =
    MongoClients.create(buildMongoUri())
      .getDatabase(Configuration.mongodbDbName)
      .getCollection(TASK_CONFIGURATION_COLLECTION_NAME, classOf[TaskConfiguration])
      .withCodecRegistry(TASK_CONFIGURATION_CODEC_REGISTRY)
}

import scala.concurrent.duration._

class FiniteDurationCodec extends Codec[FiniteDuration] {
  override def encode(writer: BsonWriter, value: FiniteDuration, encoderContext: EncoderContext): Unit = {
    writer.writeInt64(value.toMillis)
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): FiniteDuration = {
    reader.readInt64().milliseconds
  }

  override def getEncoderClass: Class[FiniteDuration] = classOf[FiniteDuration]
}

class FiniteDurationCodecProvider extends CodecProvider {
  override def get[T](clazz: Class[T], registry: CodecRegistry): Codec[T] = {
    if (classOf[FiniteDuration].isAssignableFrom(clazz)) {
      val codec = new FiniteDurationCodec
      codec.asInstanceOf[Codec[T]]
    } else {
      null
    }
  }
}
