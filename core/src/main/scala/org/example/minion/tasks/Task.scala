package org.example.minion.tasks

trait Task[RESULT] extends Serializable {
  def execute: RESULT
  def kind: String = this.getClass.getCanonicalName
}
