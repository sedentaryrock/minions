package org.example

import akka.actor.ActorSystem
import express.Express


object Main {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val messageRepository: MessageRepository = new MessageRepositoryUsingMongo
    val queueManagerService: QueueManager = new QueueManagerService

    val app = new Express
    app.bind(new HttpApi(queueManagerService))
    app.listen(9090)
  }
}