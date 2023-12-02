package org.example

import express.DynExpress
import express.http.RequestMethod
import express.http.request.Request
import express.http.response.Response

import java.util.UUID

class HttpApi(queueManagerService: QueueManager) {
  @DynExpress(method = RequestMethod.POST, context = "/queue-manager")
  def queue(req: Request, res: Response) = {
    val eventualMessage = queueManagerService.queue(UUID.randomUUID().toString, "TOPIC", "CREATED")
    val value = eventualMessage.value
    res.send("Message: " + value.get.getOrElse(None))
  }
}
