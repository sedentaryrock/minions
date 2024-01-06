package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.helpers.TaskLocator
import org.example.minion.tasks.{SampleTask, SampleTaskBuilder, SampleTaskFactory, Task, TaskBuilder, TaskFactory}
import org.example.model.Message

import scala.concurrent.duration.DurationInt

object Minion {
  private var transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo

  def main(args: Array[String]): Unit = {
    TaskLocator.locateTasks()

    val option: Option[TaskFactory[_ <: TaskBuilder[_]]] = TaskLocator.getFactory("Sample Task Factory")
    val sampleTaskBuilder:SampleTaskBuilder = option.get.getTaskBuilder.asInstanceOf[SampleTaskBuilder]
    sampleTaskBuilder.message("Sample Task Message")
    val task:SampleTask = sampleTaskBuilder.build

    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    Source.tick(0.second, 3.seconds, "Tick")
      .map(_ => {
        transitionManagerRepository.transition("TOPIC", "CREATED", "PICKED")
      })
      .flatMapConcat(publisher => Source.fromPublisher(publisher))
      .map(message => {
        (message, task.execute)
      }).runWith(
        Sink.foreach[(Message, Any)](m => {
          println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
          println(s"Message: ${m._1},\nTask outcome: ${m._2}")
          println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
        })
      )


  }

}
