package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.model.Message

import scala.concurrent.Future

class QueueManagerService(implicit actorSystem: ActorSystem, messageRepository: MessageRepository) extends QueueManager {
  override def queue(messageId: String, topic: String, status: String): Future[Message] = {
    Source.fromPublisher(messageRepository.queue(messageId, topic, status)).runWith(Sink.head[Message])
  }
}