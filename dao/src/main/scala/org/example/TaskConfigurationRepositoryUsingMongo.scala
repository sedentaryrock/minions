package org.example

import org.example.helpers.MongoCommon
import org.example.minion.tasks.Task
import org.example.model.TaskConfiguration
import org.mongodb.scala.BoxedPublisher
import org.mongodb.scala.model.Filters.equal
import org.reactivestreams.Publisher

import scala.concurrent.duration.FiniteDuration

class TaskConfigurationRepositoryUsingMongo extends TaskConfigurationRepository {
  def create(startupDelay: FiniteDuration, pollDuration: FiniteDuration, topic: String, fromStatus: String, toStatus: String, task: Option[Array[Byte]], taskClass: Option[String]): Publisher[TaskConfiguration] = {
    MongoCommon.TASK_CONFIGURATION_COLLECTION.insertOne(new TaskConfiguration(startupDelay, pollDuration, topic, fromStatus, toStatus, task, taskClass))
      .map(insertResult =>
        MongoCommon.TASK_CONFIGURATION_COLLECTION.find(equal("_id", insertResult.getInsertedId))
      )
      .flatMap(TaskConfiguration => TaskConfiguration)
  }

  override def get(cls: Class[_ <: Task[_]]): Publisher[TaskConfiguration] = {
    MongoCommon.TASK_CONFIGURATION_COLLECTION.find(equal("taskClass", cls.getCanonicalName)).first()
  }
}
