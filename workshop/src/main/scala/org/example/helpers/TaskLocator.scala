package org.example.helpers

import org.example.minion.tasks.TaskFactory
import org.reflections.Reflections

object TaskLocator {
  def main(args: Array[String]): Unit = {
    val reflections = new Reflections("org.example.minion.tasks")
    val set = reflections.getSubTypesOf[TaskFactory[_]](classOf[TaskFactory[_]])

    println(set)
  }
}