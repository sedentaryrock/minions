package org.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.example.helpers.{StdSerializationHelper, TaskLocator}
import org.example.minion.tasks.{SampleTask, Task}
import org.example.model.{Message, TaskConfiguration}
import org.mongodb.scala.{BoxedPublisher, Observable}

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.time.Instant
import java.util.Base64
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Minion {
  private val transitionManagerRepository: TransitionManagerRepository = new TransitionManagerRepositoryUsingMongo
  private val messageRepository: MessageRepository = new MessageRepositoryUsingMongo
  private val taskConfigurationRepository: TaskConfigurationRepository = new TaskConfigurationRepositoryUsingMongo

  def main(args: Array[String]): Unit = {
    println("Serialized bytes are " + serializeToBase64(new SampleTask("Sample TASK")))

    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    val optionTask: Option[Class[_ <:Task[_]]] = TaskLocator.getTaskClass("org.example.minion.tasks")
    val taskConfigurationObservable: Observable[TaskConfiguration] =  taskConfigurationRepository.get(optionTask.getOrElse(classOf[SampleTask])).toObservable()

    val taskConfiguration: Seq[TaskConfiguration] = Await.result(taskConfigurationObservable.toFuture(), 10.seconds)

    taskConfiguration.foreach(configuration => {
      println(s"Working on ${configuration}")

      val result: Option[Task[_]] = deserializeTask(configuration.task, configuration.taskClass)

      result match {
        case None =>
          println("Task could not be deserialized")

        case Some(task) =>
          Source.tick(configuration.startupDelay, configuration.pollDuration, "Tick")
            .map(tick => {
              val now = Instant.now
              println(s"Ticking ticks at $now")
              tick
            })
            .map(_ => transitionManagerRepository.pickUp("TOPIC", "CREATED"))
            .flatMapConcat(publisher => Source.fromPublisher(publisher))

            .map(message => {
              val output = task.execute
              println(s"Task executed with output as $output")
              (message, StdSerializationHelper.serialize(output))
            })
            .map(tuple => {
              println(s"Updating message with _id: ${tuple._1}")
              messageRepository.updateOutput(tuple._1._id.toString, tuple._2)
            })
            .flatMapConcat(publisher => Source.fromPublisher(publisher))

            .map(message => {
              println(s"Putting down message with _id: ${message._id}")
              transitionManagerRepository.putDown(message._id.toString, "PICKED")
            })
            .flatMapConcat(publisher => Source.fromPublisher(publisher))

            .log("################## Failed Failed Failed #################")
            .runWith(
              Sink.foreach[Message](m => {
                println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
                println(s"Message: $m")
                println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
              })
            )
      }
    });
  }


  import java.io.{ByteArrayInputStream, ObjectInputStream}
  private def deserializeTask(byteArrayOpt: Option[Array[Byte]], classNameOpt: Option[String]): Option[Task[_]] = {
    for {
      byteArray <- byteArrayOpt
      className <- classNameOpt
    } yield {
      try {
        // Load the class dynamically
        val classObj = Class.forName(className)

        // Deserialize the byte array
        val byteArrayInputStream = new ByteArrayInputStream(byteArray)
        val objectInputStream = new ObjectInputStream(byteArrayInputStream)
        val deserializedObject = objectInputStream.readObject()
        objectInputStream.close()

        // Use PartialFunction.condOpt to filter and cast to Task[_]
        PartialFunction.condOpt(deserializedObject) {
          case task: Task[_] => task
        }
      } catch {
        case e: ClassNotFoundException =>
          println(s"Class not found: $className")
          None
        case e: java.io.IOException =>
          println(s"Error reading byte array: ${e.getMessage}")
          None
      }
    }
  }.flatten // Flatten the Option[Option[Task[_]]] to Option[Task[_]]

  private def serializeToBase64(obj: Serializable): Option[String] = {
    try {
      val byteArrayOutputStream = new ByteArrayOutputStream()
      val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
      objectOutputStream.writeObject(obj)
      objectOutputStream.close()

      val byteArray = byteArrayOutputStream.toByteArray
      Some(Base64.getEncoder.encodeToString(byteArray))
    } catch {
      case e: Exception =>
        println(s"Serialization failed: ${e.getMessage}")
        None
    }
  }
}
