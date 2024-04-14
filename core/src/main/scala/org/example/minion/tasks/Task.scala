package org.example.minion.tasks

trait Task[RESULT] {
  def execute: RESULT
  def kind: String = this.getClass.getCanonicalName
}
