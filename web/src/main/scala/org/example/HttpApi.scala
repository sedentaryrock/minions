package org.example

import express.DynExpress
import express.http.RequestMethod
import express.http.request.Request
import express.http.response.Response
import org.example.model.Message

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpApi(queueManagerService: QueueManager) {
  @DynExpress(method = RequestMethod.POST, context = "/queue-manager")
  def queue(req: Request, res: Response) = {
    val future: Future[Message] = queueManagerService.queue(UUID.randomUUID().toString, "TOPIC", "CREATED")

    future.onComplete(message => {
      res.send("Message: " + message)
    })
  }
}
