//package org.example
//
//import akka.actor.ActorSystem
//import akka.stream.alpakka.mongodb.scaladsl.MongoSink
//import akka.stream.scaladsl.Source
//import com.mongodb.reactivestreams.client.MongoClients
//import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
//import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
//import org.example.model.Message
//import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
//import org.mongodb.scala.bson.codecs.Macros
//
//import scala.concurrent.ExecutionContext.Implicits.global
//
//object Main {
////  private implicit val system: ActorSystem = ActorSystem()
////
////  def main(args: Array[String]): Unit = {
////    implicit val codecProvider: CodecProvider = Macros.createCodecProviderIgnoreNone[Message]()
////    implicit val codecRegistry: CodecRegistry =
////      fromRegistries(fromProviders(codecProvider), DEFAULT_CODEC_REGISTRY)
////    val mongoClient = MongoClients.create("mongodb://localhost:27017")
////    val db = mongoClient.getDatabase("TEST")
////    val messagesCollection = db
////      .getCollection("messages", classOf[Message]).withCodecRegistry(codecRegistry)
////    val mesg = model.Message("SOMEID", "TEST-TOPIC-1", "NEW")
////
////    println("Message: " + mesg)
////
////
////    val eventualDone = Source.single(mesg).runWith(MongoSink.insertOne[model.Message](messagesCollection))
////    eventualDone.onComplete(_ => {
////      println("################## Terminating Stream #############")
////      system.terminate()
////      mongoClient.close()
////    })
////  }
//}
