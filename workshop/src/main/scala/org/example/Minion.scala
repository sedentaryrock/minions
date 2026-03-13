package org.example

import org.apache.pekko.actor.ActorSystem
import org.example.helpers.TaskLocator
import org.example.minion.tasks.{SampleTask, Task}
import org.example.model.TaskConfiguration
import org.mongodb.scala.{BoxedPublisher, Observable}
import org.slf4j.{Logger, LoggerFactory}

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.util.Base64
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * Minion executor - runs as a background service to process messages through
 * configured task pipelines. Each task configuration defines a source (topic/status),
 * the task to execute, and the destination status.
 */
object Minion {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  // ...existing code...
  private val transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo
  private val messageRepository: MessageRepository = new MessageRepositoryUsingMongo
  private val taskConfigurationRepository: TaskConfigurationRepository = new TaskConfigurationRepositoryUsingMongo
  private val minionService: MinionService = new MinionService(transitionManagerRepository, messageRepository)

  def main(args: Array[String]): Unit = {
    logger.info("Starting Minion service...")

    val sampleTaskSerialized = serializeToBase64(new SampleTask("Sample TASK"))
    logger.debug(s"Sample task serialization: $sampleTaskSerialized")

    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)

    // Load task configurations for the sample task
    val optionTask: Option[Class[_ <: Task[_]]] = TaskLocator.getTaskClass("org.example.minion.tasks.SampleTask")
    val taskConfigurationObservable: Observable[TaskConfiguration] = taskConfigurationRepository
      .get(optionTask.getOrElse(classOf[SampleTask]).getCanonicalName)
      .toObservable()

    val taskConfigurations: Seq[TaskConfiguration] = Await.result(taskConfigurationObservable.toFuture(), 10.seconds)

    if (taskConfigurations.isEmpty) {
      logger.warn("No task configurations found. Minion has nothing to do.")
      return
    }

    logger.info(s"Found ${taskConfigurations.length} task configuration(s)")

    // Start processing for each task configuration
    taskConfigurations.foreach(configuration => {
      logger.info(s"Setting up processor for topic=${configuration.topic}, fromStatus=${configuration.fromStatus}, toStatus=${configuration.toStatus}")

      val result: Option[Task[_]] = minionService.deserializeTask(configuration.task, configuration.taskClass)

      result match {
        case None =>
          logger.error(s"Failed to deserialize task for configuration: $configuration")

        case Some(task) =>
          logger.info(s"Task deserialized successfully: ${task.kind}")
          minionService.processTaskConfiguration(configuration, task)
      }
    })

    logger.info("Minion initialization complete. Message processing streams are now active.")
  }

  /**
   * Serializes an object to Base64 string for storage/transmission.
   *
   * @param obj object to serialize
   * @return Some(base64String) if successful, None otherwise
   */
  private def serializeToBase64(obj: Serializable): Option[String] = {
    try {
      val byteArrayOutputStream = new ByteArrayOutputStream()
      val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
      objectOutputStream.writeObject(obj)
      objectOutputStream.close()

      val byteArray = byteArrayOutputStream.toByteArray
      Some(Base64.getEncoder.encodeToString(byteArray))
    } catch {
      case e: Exception =>
        logger.error(s"Serialization failed: ${e.getMessage}", e)
        None
    }
  }
}
