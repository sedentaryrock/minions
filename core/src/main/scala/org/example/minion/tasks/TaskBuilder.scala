package org.example.minion.tasks

trait TaskBuilder[T <: Task[_]] {
  def build[Map[String, Object]]: T
}
