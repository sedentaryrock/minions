package org.example.minion.tasks

class DemoTask extends Task[String] {
  override def execute: String = "Demo output"

  override def name: String = "Demo Task"
}
