package org.example.minion.tasks

class SampleTask(val message: String) extends Task[String] {
  override def execute: String = message

  override def name: String = "Sample Task"
}
