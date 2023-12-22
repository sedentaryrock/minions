package org.example.minion.tasks

class SampleTask extends Task[String] {
  override def execute: String = "Sample output"
  override def name: String = "Sample Task"
}
