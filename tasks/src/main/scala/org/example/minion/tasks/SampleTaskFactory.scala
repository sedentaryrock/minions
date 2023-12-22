package org.example.minion.tasks

object SampleTaskFactory extends TaskFactory[SampleTask]{
  override def getInstance: Task[_] = new SampleTask
}
