package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.helpers.TaskLocator
import org.example.minion.tasks.{SampleTask, Task, TaskFactory}
import org.example.model.Message

import scala.concurrent.duration.DurationInt

object Minion {
  private var transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo

  def main(args: Array[String]): Unit = {
//    val task: Task[String] = new Task[String] {
//      override def execute: String = "Sample"
//      override def name: String = "Sample Task"
//    }

    TaskLocator.locateTasks()
    val option: Option[TaskFactory[SampleTask]] = TaskLocator.getFactory[SampleTask]
    val instance = option.get.getInstance

//    val option: Option[TaskFactory[_]] = TaskLocator.map.get(classOf[SampleTask])
//
//    val task:TaskFactory[_] = option.get.getInstance





//    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
//
//    Source.tick(0.second, 3.seconds, "Tick")
//      .map(_ => {
//        transitionManagerRepository.transition("TOPIC", "CREATED", "PICKED")
//      })
//      .flatMapConcat(publisher => Source.fromPublisher(publisher))
//      .map(message => {
//        (message, task.execute)
//      }).runWith(
//        Sink.foreach[(Message, Any)](m => {
//          println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
//          println(s"Message: ${m._1}, Task outcome: ${m._2}")
//          println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
//        })
//      )


  }

}
