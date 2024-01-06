package org.example.minion.tasks

import org.example.Identifiable

trait TaskFactory[T <: TaskBuilder[_]] extends Identifiable {
  def getTaskBuilder: T
}