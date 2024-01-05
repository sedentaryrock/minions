package org.example

import scala.annotation.unused

object Configuration {


  // Server
  private val DEFAULT_SERVER_PORT = 9090

  // Akka Streams
  private val DEFAULT_ACTOR_SYSTEM_NAME = "defaultActorSystem"

  // MongoDB
  private val DEFAULT_MONGODB_URL = "mongodb://localhost:27017"
  private val DEFAULT_MONGODB_DB_NAME = "TEST"


  // TODO: Read values from configuration properties or typesafe config file
  /*
    private val url = getClass.getResource("application.properties")
    private val properties: Properties = new Properties()

    if (url != null) {
      val source = Source.fromURL(url)
      properties.load(source.bufferedReader())
    }
    else {
      throw new FileNotFoundException("Properties file cannot be loaded")
    }
  */

  private def readConfig[T](@unused configKey: String, defaultValue: T): T = {
    defaultValue
  }

  val SERVER_PORT: Int = readConfig("server.port", DEFAULT_SERVER_PORT)
  val mongodbUrl: String = readConfig("mongodb-url", DEFAULT_MONGODB_URL)
  val mongodbDbName: String = readConfig("mongodbDbName", DEFAULT_MONGODB_DB_NAME)
  val actorSystemName: String = readConfig("actorSystemName", DEFAULT_ACTOR_SYSTEM_NAME)
}
