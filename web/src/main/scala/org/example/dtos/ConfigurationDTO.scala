package org.example.dtos

import scala.concurrent.duration.FiniteDuration


case class ConfigurationDTO(
                             startupDelay: FiniteDuration
                             , pollDuration: FiniteDuration
                             , topic: String
                             , fromStatus: String
                             , toStatus: String
                             , task: String
                           )
