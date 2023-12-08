package org.example

import express.DynExpress
import express.http.RequestMethod
import express.http.request.Request
import express.http.response.Response
import express.utils.{MediaType, Status}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.example.dtos.QueueRequestDTO
import org.example.model.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpApi(queueManagerService: QueueManager) {

  @DynExpress(method = RequestMethod.POST, context = "/queue-manager")
  def queue(req: Request, res: Response) = {
    val queueRequestDTO: Either[io.circe.Error, QueueRequestDTO] =
      decode[QueueRequestDTO](req.getMiddlewareContent(BodyParserMiddleware.NAME).toString)

    queueRequestDTO match {
      case Right(qrd) =>
        val future: Future[Message] = queueManagerService.queue(qrd.messageId, qrd.topic, qrd.status)
        future.onComplete(message => {
          val responseBody: String = QueueRequestDTO.fromMessage(message.get).asJson.noSpaces

          res.setStatus(Status._201)
          res.setContentType(MediaType._json)
          res.send(responseBody)
        })

      case Left(error) =>
        println(s"Error decoding JSON: $error")
        res.sendStatus(Status._400)
    }


  }
}
