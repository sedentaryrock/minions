package org.example.minion.tasks

class SampleTaskFactory extends TaskFactory[SampleTaskBuilder] {
  override def name: String = "Sample Task Factory"

  override def getTaskBuilder: SampleTaskBuilder = new SampleTaskBuilder()
}
