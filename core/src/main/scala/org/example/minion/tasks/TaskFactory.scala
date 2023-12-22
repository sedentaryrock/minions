package org.example.minion.tasks

trait TaskFactory[T <: Task[_]] {
  def getInstance: Task[_]
}