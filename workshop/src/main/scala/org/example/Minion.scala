package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.helpers.TaskLocator
import org.example.minion.tasks.{SampleTask, SampleTaskBuilder, SampleTaskFactory}
import org.example.model.Message

import scala.concurrent.duration.DurationInt

object Minion {
  private val transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo

  def main(args: Array[String]): Unit = {
    TaskLocator.locateTasks()

    val someFactory: Option[SampleTaskFactory] = TaskLocator.getFactory[SampleTaskFactory]("Sample Task Factory")
    val sampleTaskBuilder:SampleTaskBuilder = someFactory.get.getTaskBuilder
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
