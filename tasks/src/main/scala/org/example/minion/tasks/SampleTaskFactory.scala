package org.example.minion.tasks

class SampleTaskFactory extends TaskFactory[SampleTaskBuilder] {
  override def getTaskBuilder: SampleTaskBuilder = new SampleTaskBuilder()

  override def name: String = "Sample Task Factory"
}
