package org.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

/**
 * Janitor executor - runs as a background service to periodically clean up
 * messages stuck in PICKED state (hung/abandoned messages).
 */
object Janitor {
  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val janitorRepository: JanitorRepository = new JanitorRepositoryUsingMongo
  private val janitorService: JanitorService = new JanitorService(janitorRepository)

  def main(args: Array[String]): Unit = {
    logger.info("Starting Janitor service...")

    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)

    // Convert minutes to FiniteDuration
    val hungJobTimeout = Configuration.hungJobTimeoutMinutes.minutes
    val janitorPollInterval = 5.minutes // Run janitor every 5 minutes

    logger.info(s"Janitor configuration: hungJobTimeout=${Configuration.hungJobTimeoutMinutes} minutes, pollInterval=${janitorPollInterval}")

    // Start a ticker that runs the cleanup every janitorPollInterval
    Source.tick(
      initialDelay = 10.seconds, // Wait 10 seconds before first run
      interval = janitorPollInterval,
      tick = "janitor-tick"
    )
      .map { _ =>
        logger.info("Janitor starting cleanup cycle...")
        val cleanedCount = janitorService.cleanupHungMessages(hungJobTimeout)
        logger.info(s"Janitor cleanup cycle completed. Marked ${cleanedCount} messages as DEAD")
        cleanedCount
      }
      .runWith(
        Sink.foreach[Long](count => {
          logger.debug(s"Janitor cycle result: $count messages cleaned up")
        })
      )
  }
}

