package org.example.minion.tasks

class SampleTaskFactory extends TaskFactory[SampleTask] {
  override def getInstance: SampleTask = new SampleTask

  override def getType: Class[SampleTask] = classOf[SampleTask]
}
