package org.example.minion.tasks

import org.example.Identifiable

trait TaskBuilder[T <: Task[_]] extends Identifiable {
  def build: T
}
