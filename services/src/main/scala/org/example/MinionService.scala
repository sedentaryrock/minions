package org.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.example.helpers.StdSerializationHelper
import org.example.minion.tasks.Task
import org.example.model.{Message, TaskConfiguration}
import org.mongodb.scala.BoxedPublisher
import org.slf4j.{Logger, LoggerFactory}

import java.io.{ByteArrayInputStream, ObjectInputStream}
import scala.concurrent.duration.FiniteDuration

/**
 * MinionService handles the processing of messages through configured task pipelines.
 * It manages the stream-based message flow: pickup → execute → update → putdown.
 */
class MinionService(
  transitionManagerRepository: TransitionManagerRepository,
  messageRepository: MessageRepository
) {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  /**
   * Processes a task configuration by setting up a continuous stream that:
   * 1. Picks up messages from the topic/status
   * 2. Executes the task on each message
   * 3. Updates the message with task output
   * 4. Puts down the message with new status
   *
   * @param configuration task configuration defining topic, status, and task
   * @param task the actual task instance to execute
   * @param actorSystem implicit actor system for streams
   */
  def processTaskConfiguration(configuration: TaskConfiguration, task: Task[_])(implicit actorSystem: ActorSystem): Unit = {
    logger.info(s"Starting message processor for topic=${configuration.topic}, fromStatus=${configuration.fromStatus}")

    Source.tick(
      initialDelay = configuration.startupDelay,
      interval = configuration.pollDuration,
      tick = "tick"
    )
      .map { _ =>
        logger.debug(s"Polling for messages: topic=${configuration.topic}, status=${configuration.fromStatus}")
        transitionManagerRepository.pickUp(configuration.topic, configuration.fromStatus)
      }
      .flatMapConcat(publisher => Source.fromPublisher(publisher))

      .map { message =>
        try {
          val output = task.execute
          logger.debug(s"Task executed successfully for message ${message._id}")
          (message, StdSerializationHelper.serialize(output))
        } catch {
          case e: Exception =>
            logger.error(s"Task execution failed for message ${message._id}: ${e.getMessage}", e)
            throw e
        }
      }

      .map { tuple =>
        logger.debug(s"Updating message ${tuple._1._id} with task output")
        messageRepository.updateOutput(tuple._1._id.toString, tuple._2)
      }
      .flatMapConcat(publisher => Source.fromPublisher(publisher))

      .map { message =>
        logger.debug(s"Transitioning message ${message._id} from status ${configuration.fromStatus} to ${configuration.toStatus}")
        transitionManagerRepository.putDown(message._id.toString, configuration.toStatus)
      }
      .flatMapConcat(publisher => Source.fromPublisher(publisher))

      .runWith(
        Sink.foreach[Message] { message =>
          logger.info(s"Message successfully processed: id=${message._id}, topic=${message.topic}, newStatus=${message.status}")
        }
      )
  }

  /**
   * Deserializes a task from byte array and class name.
   *
   * @param byteArrayOpt serialized task bytes
   * @param classNameOpt fully qualified class name
   * @return Some(Task) if deserialization succeeds, None otherwise
   */
  def deserializeTask(byteArrayOpt: Option[Array[Byte]], classNameOpt: Option[String]): Option[Task[_]] = {
    for {
      byteArray <- byteArrayOpt
      className <- classNameOpt
    } yield {
      try {
        Class.forName(className)

        val byteArrayInputStream = new ByteArrayInputStream(byteArray)
        val objectInputStream = new ObjectInputStream(byteArrayInputStream)
        val deserializedObject = objectInputStream.readObject()
        objectInputStream.close()

        PartialFunction.condOpt(deserializedObject) {
          case task: Task[_] => task
        }
      } catch {
        case e: ClassNotFoundException =>
          logger.error(s"Task class not found: $className", e)
          None
        case e: java.io.IOException =>
          logger.error(s"Error deserializing task from byte array: ${e.getMessage}", e)
          None
      }
    }
  }.flatten
}

