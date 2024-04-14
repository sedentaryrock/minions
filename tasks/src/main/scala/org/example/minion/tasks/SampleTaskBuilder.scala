package org.example.minion.tasks

class SampleTaskBuilder(val message: String = "") extends TaskBuilder[SampleTask] {
  override def build[Map[String, Object]]: SampleTask = {
    new SampleTask("")
  }
}
