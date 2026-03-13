package org.example

import org.example.minion.tasks.Task
import org.example.model.TaskConfiguration
import org.reactivestreams.Publisher

import scala.concurrent.duration.FiniteDuration

trait TaskConfigurationRepository {
  type TaskClass = Class[_ <: Task[_]]

  def create(startupDelay: FiniteDuration, pollDuration: FiniteDuration, topic: String, fromStatus: String, toStatus: String, task: Option[Array[Byte]], taskClass: Option[String]): Publisher[TaskConfiguration]
  def get(cls: String): Publisher[TaskConfiguration]
}