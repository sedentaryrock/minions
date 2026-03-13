package org.example

import org.example.model.Message
import org.mongodb.scala.BoxedPublisher
import org.slf4j.{Logger, LoggerFactory}

import java.time.Instant
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Await

/**
 * Janitor service responsible for cleaning up hung/abandoned messages.
 * Identifies messages stuck in PICKED state beyond the configured timeout
 * and marks them as DEAD for recovery.
 */
class JanitorService(janitorRepository: JanitorRepository) {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  /**
   * Scans for messages stuck in PICKED state beyond the timeout duration
   * and marks them as DEAD.
   *
   * @param hungJobTimeout timeout duration for considering a job as hung
   * @return count of messages marked as DEAD
   */
  def cleanupHungMessages(hungJobTimeout: FiniteDuration): Long = {
    val now = Instant.now()
    val cutoffTime = Instant.ofEpochMilli(now.toEpochMilli - hungJobTimeout.toMillis)

    logger.info(s"Janitor scanning for messages picked before ${cutoffTime}")

    try {
      val hungMessagesPublisher = janitorRepository.fetchHungMessages(cutoffTime)
      val hungMessagesObservable = new BoxedPublisher(hungMessagesPublisher).toObservable()

      val hungMessages: Seq[Message] = Await.result(
        hungMessagesObservable.toFuture(),
        hungJobTimeout
      )

      logger.info(s"Janitor found ${hungMessages.length} hung messages")

      hungMessages.foreach(msg => {
        logger.warn(s"Marking message ${msg._id} as DEAD - picked at ${msg.createdOn} (cutoff: ${cutoffTime})")
        val deadMessagePublisher = janitorRepository.markMessageAsDead(msg._id.toString)
        val deadMessageObservable = new BoxedPublisher(deadMessagePublisher).toObservable()
        Await.result(deadMessageObservable.toFuture(), hungJobTimeout)
      })

      hungMessages.length
    } catch {
      case e: Exception =>
        logger.error(s"Error during Janitor cleanup: ${e.getMessage}", e)
        0
    }
  }
}



