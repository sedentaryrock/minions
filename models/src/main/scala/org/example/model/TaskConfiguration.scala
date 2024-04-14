package org.example.model

import scala.concurrent.duration.FiniteDuration

case class TaskConfiguration(
                              startupDelay: FiniteDuration
                              , pollDuration: FiniteDuration
                              , topic: String
                              , fromStatus: String
                              , toStatus: String
                              , task: Option[Array[Byte]]
                              , taskClass: Option[String]
                            )
