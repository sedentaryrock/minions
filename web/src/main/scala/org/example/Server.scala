package org.example

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.example.dtos.QueueRequestDTO
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object Server {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem.create(Configuration.actorSystemName)
    implicit val messageRepository: QueueManagerRepository = new QueueManagerRepositoryUsingMongo
    val queueManagerService: QueueManager = new QueueManagerService

    val route =
      path("queue-manager") {
        post {
          entity(as[String]) { body =>
            decode[QueueRequestDTO](body) match {
              case Right(qrd) =>
                val future = queueManagerService.queue(qrd.messageId, qrd.topic, qrd.status)
                onComplete(future) { attempt =>
                  attempt match {
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
                }
              case Left(error) =>
                logger.error(s"Error decoding JSON: $error")
                complete(HttpResponse(status = StatusCodes.BadRequest))
            }
          }
        }
      }

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
}