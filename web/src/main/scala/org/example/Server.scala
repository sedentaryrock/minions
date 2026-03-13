package org.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.example.dtos.{QueueRequestDTO, TaskConfigurationDTO}
import org.example.model.TaskConfiguration
import org.example.TaskConfigurationRepository
import org.example.TaskConfigurationRepositoryUsingMongo
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object Server {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    implicit val messageRepository: QueueManagerRepository = new QueueManagerRepositoryUsingMongo
    val queueManagerService: QueueManager = new QueueManagerService
    val taskConfigurationRepository: TaskConfigurationRepository = new TaskConfigurationRepositoryUsingMongo

    val queueRoute =
      path("queue-manager") {
        post {
          entity(as[String]) { body =>
            decode[QueueRequestDTO](body) match {
              case Right(qrd) =>
                val future = queueManagerService.queue(qrd.messageId, qrd.topic, qrd.status)
                onComplete(future) {
                  case scala.util.Success(message) =>
                    val responseBody = QueueRequestDTO.fromMessage(message).asJson.noSpaces
                    complete(HttpResponse(
                      status = StatusCodes.Created,
                      entity = HttpEntity(ContentTypes.`application/json`, responseBody)
                    ))
                  case scala.util.Failure(error) =>
                    logger.error(s"Error processing queue request: $error")
                    complete(HttpResponse(status = StatusCodes.InternalServerError))
                }
              case Left(error) =>
                logger.error(s"Error decoding JSON: $error")
                complete(HttpResponse(status = StatusCodes.BadRequest))
            }
          }
        }
      }

    val configurationRoute =
      path("configurations") {
        post {
          entity(as[String]) { body =>
            decode[TaskConfigurationDTO](body) match {
              case Right(configDto) =>
                val publisher = taskConfigurationRepository.create(
                  configDto.startupDelay,
                  configDto.pollDuration,
                  configDto.topic,
                  configDto.fromStatus,
                  configDto.toStatus,
                  Option(configDto.task),
                  Option(configDto.taskClass)
                )
                onComplete(publisherToFuture(publisher)) {
                  case scala.util.Success(taskConfig) =>
                    val responseBody = TaskConfigurationDTO.fromTaskConfiguration(taskConfig).asJson.noSpaces
                    complete(HttpResponse(
                      status = StatusCodes.Created,
                      entity = HttpEntity(ContentTypes.`application/json`, responseBody)
                    ))
                  case scala.util.Failure(error) =>
                    logger.error(s"Error creating configuration: $error")
                    complete(HttpResponse(status = StatusCodes.InternalServerError))
                }
              case Left(error) =>
                logger.error(s"Error decoding JSON: $error")
                complete(HttpResponse(status = StatusCodes.BadRequest))
            }
          }
        } ~
        get {
          complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Use POST to create a new configuration or GET /configurations/{taskClass} to retrieve a configuration.")))
        }
      } ~
      path("configurations" / Segment) { taskClassName =>
        get {
          val publisher = taskConfigurationRepository.get(taskClassName)
          onComplete(publisherToFuture(publisher)) {
            case scala.util.Success(taskConfig) =>
              val responseBody = TaskConfigurationDTO.fromTaskConfiguration(taskConfig).asJson.noSpaces
              complete(HttpResponse(
                status = StatusCodes.OK,
                entity = HttpEntity(ContentTypes.`application/json`, responseBody)
              ))
            case scala.util.Failure(error) =>
              logger.error(s"Error retrieving configuration: $error")
              complete(HttpResponse(status = StatusCodes.NotFound))
          }
        }
      }

    val route = queueRoute ~ configurationRoute

    val bindingFuture = Http().newServerAt("localhost", Configuration.SERVER_PORT).bind(route)

    bindingFuture.foreach { binding =>
      println(s"Server is listening on ${binding.localAddress.getHostString}:${binding.localAddress.getPort}")
    }

    bindingFuture.failed.foreach { ex =>
      logger.error(s"Binding failed: ${ex.getMessage}", ex)
      actorSystem.terminate()
    }

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => actorSystem.terminate())
  }

  private def publisherToFuture[T](publisher: org.reactivestreams.Publisher[T]): scala.concurrent.Future[T] = {
    val promise = scala.concurrent.Promise[T]()
    publisher.subscribe(new org.reactivestreams.Subscriber[T] {
      override def onNext(item: T): Unit = promise.trySuccess(item)
      override def onError(throwable: Throwable): Unit = promise.tryFailure(throwable)
      override def onComplete(): Unit = ()
      override def onSubscribe(s: org.reactivestreams.Subscription): Unit = s.request(1)
    })
    promise.future
  }
}