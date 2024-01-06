package org.example.minion.tasks

class SampleTaskBuilder(var message: String = "") extends TaskBuilder[SampleTask] {
  def message(msg: String): SampleTaskBuilder = {
    message = msg
    this
  }

  override def name: String = "Sample Task Builder"
  override def build: SampleTask = new SampleTask(this.message)
}
