package org.example.helpers

import org.example.minion.tasks.TaskFactory
import org.reflections.Reflections

import scala.jdk.CollectionConverters.SetHasAsScala
import scala.reflect.ClassTag

object TaskLocator {
  private var map: Map[String, TaskFactory[_]] = Map.empty

  def locateTasks(): Unit = {
    val reflections = new Reflections("org.example.minion.tasks")
    val set = reflections.getSubTypesOf[TaskFactory[_]](classOf[TaskFactory[_]])

    map = set.asScala
      .map(clazz => clazz.getDeclaredConstructor().newInstance())
      .map(factoryInstance => (factoryInstance.name, factoryInstance))
      .toMap
  }

  def getFactory[T <: TaskFactory[_] : ClassTag](key: String): Option[T] = {
    map.get(key) match {
      case Some(value) if implicitly[ClassTag[T]].runtimeClass.isInstance(value) =>
        Some(value.asInstanceOf[T])
      case _ => None
    }
  }
}