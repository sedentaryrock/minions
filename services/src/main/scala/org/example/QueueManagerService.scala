package org.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Keep, Sink, Source}
import org.example.model.Message

import scala.concurrent.Future

class QueueManagerService(implicit actorSystem: ActorSystem, messageRepository: QueueManagerRepository) extends QueueManager {
  override def queue(messageId: String, topic: String, status: String): Future[Message] = {
    Source.fromPublisher[Message](messageRepository.queue(messageId, topic, status)).toMat(Sink.head[Message])(Keep.right).run()
  }
}