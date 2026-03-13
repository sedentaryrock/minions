package org.example

import org.example.model.Message
import org.reactivestreams.Publisher

import java.time.Instant

trait JanitorRepository {
  /**
   * Fetches all messages that are in PICKED state (have PICKED field set)
   * but have been stuck since before the given cutoff time.
   *
   * @param cutoffTime messages picked before this time are considered hung
   * @return Publisher containing hung messages
   */
  def fetchHungMessages(cutoffTime: Instant): Publisher[Message]

  /**
   * Marks a hung message (stuck in PICKED state beyond timeout) as DEAD
   * and unsets the PICKED flag to prevent reprocessing.
   *
   * @param _id message ID
   * @return Publisher containing the updated message
   */
  def markMessageAsDead(_id: String): Publisher[Message]
}


