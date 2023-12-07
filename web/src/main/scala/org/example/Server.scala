package org.example

import akka.actor.ActorSystem
import express.Express


object Server {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    implicit val messageRepository: MessageRepository = new MessageRepositoryUsingMongo
    val queueManagerService: QueueManager = new QueueManagerService

    val app = new Express
    app.bind(new HttpApi(queueManagerService))
    app.listen(9090)
  }
}