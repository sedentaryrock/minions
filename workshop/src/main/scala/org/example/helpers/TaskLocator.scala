package org.example.helpers

import org.example.minion.tasks.Task
import org.reflections.Reflections

import scala.jdk.CollectionConverters.SetHasAsScala

object TaskLocator {
  private val taskMap: Map[String, Class[_<: Task[_]]] = {
    val reflections = new Reflections("org.example.minion.tasks")
    val set = reflections.getSubTypesOf[Task[_]](classOf[Task[_]])

    set.asScala
      .map(clazz => (clazz.getCanonicalName, clazz))
      .toMap
  }

  def getTaskClass(key: String) = {
    taskMap.get(key)
  }
}