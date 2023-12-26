package org.example.helpers

import org.example.minion.tasks.{Task, TaskFactory}
import org.reflections.Reflections

import scala.jdk.CollectionConverters.SetHasAsScala
import scala.reflect.{ClassTag, classTag}

object TaskLocator {
  private var map: Map[Class[_], TaskFactory[_]] = Map.empty

  def locateTasks(): Unit = {
    val reflections = new Reflections("org.example.minion.tasks")
    val set = reflections.getSubTypesOf[TaskFactory[_]](classOf[TaskFactory[_]])

    map = set.asScala
      .map(clazz => clazz.getDeclaredConstructor().newInstance())
      .map(factoryInstance => (factoryInstance.getType, factoryInstance))
      .toMap
  }

  def getFactory[T <: Task[_] : ClassTag]: Option[TaskFactory[T]] = {
    map.get(classTag[T].runtimeClass).map(_.asInstanceOf[TaskFactory[T]])
  }
}