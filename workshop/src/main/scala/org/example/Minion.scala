package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.helpers.{StdSerializationHelper, TaskLocator}
import org.example.minion.tasks.{SampleTask, Task}
import org.example.model.{Message, TaskConfiguration}
import org.mongodb.scala.{BoxedPublisher, Observable}

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Minion {
  private val transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo
  private val messageRepository: MessageRepository = new MessageRepositoryUsingMongo
  private val taskConfigurationRepository: TaskConfigurationRepository = new TaskConfigurationRepositoryUsingMongo

  def main(args: Array[String]): Unit = {
    val optionTask: Option[Class[_ <:Task[_]]] = TaskLocator.getTaskClass("org.example.minion.tasks")
    val taskConfigurationObservable: Observable[TaskConfiguration] =  taskConfigurationRepository.get(optionTask.getOrElse(classOf[SampleTask])).toObservable()

    val taskConfiguration: Seq[TaskConfiguration] = Await.result(taskConfigurationObservable.toFuture(), 10.seconds)

    taskConfiguration.foreach(configuration => {
      println(s"Working on ${configuration}")
//      val task: SampleTask = new SampleTask("Sample");
//
//      implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
//      Source.tick(0.second, 3.seconds, "Tick")
//        .map(tick => {
//          val now = Instant.now
//          println(s"Ticking ticks at $now")
//          tick
//        })
//        .map(_ => transitionManagerRepository.pickUp("TOPIC", "CREATED"))
//        .flatMapConcat(publisher => Source.fromPublisher(publisher))
//
//        .map(message => {
//          val output = task.execute
//          println(s"Task executed with output as $output")
//          (message, StdSerializationHelper.serialize(output))
//        })
//        .map(tuple => {
//          println(s"Updating message with _id: ${tuple._1}")
//          messageRepository.updateOutput(tuple._1._id.toString, tuple._2)
//        })
//        .flatMapConcat(publisher => Source.fromPublisher(publisher))
//
//        .map(message => {
//          println(s"Putting down message with _id: ${message._id}")
//          transitionManagerRepository.putDown(message._id.toString, "PICKED")
//        })
//        .flatMapConcat(publisher => Source.fromPublisher(publisher))
//
//        .log("################## Failed Failed Failed #################")
//        .runWith(
//          Sink.foreach[Message](m => {
//            println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
//            println(s"Message: $m")
//            println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
//          })
//        )
    });
  }

}
