package org.example

import akka.actor.ActorSystem
import express.Express


object Server {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    implicit val messageRepository: QueueManagerRepository = new QueueManagerRepositoryUsingMongo
    val queueManagerService: QueueManager = new QueueManagerService

    val app = new Express
    val bodyParser = new BodyParserMiddleware
    app.use("*", "POST", bodyParser)
    app.use("*", "PUT", bodyParser)
    app.use("*", "PATCH", bodyParser)
    app.bind(new HttpApi(queueManagerService))

    app.listen(9090)
  }
}