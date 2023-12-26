package org.example.minion.tasks

import org.example.Identifiable

trait TaskFactory[T <: Task[_]] extends Identifiable {
  def getInstance: T
}