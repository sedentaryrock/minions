package org.example.minion.tasks

trait TaskFactory[T <: TaskBuilder[_]] {
  def getTaskBuilder: T
}