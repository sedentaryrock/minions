package org.example

import express.filter.Filter
import express.http.HttpRequestHandler
import express.http.request.Request
import express.http.response.Response
import org.example.BodyParserMiddleware.NAME

import java.io.InputStream
import scala.io.Source

object BodyParserMiddleware {
  val NAME = "BODY_PARSER_MIDDLEWARE"
}

class BodyParserMiddleware extends HttpRequestHandler with Filter {


  override def handle(req: Request, res: Response): Unit = {
    val stream = req.getMethod.toLowerCase match {
      case "post" | "put" | "patch" => req.getBody
      case _ => InputStream.nullInputStream
    }

    req.addMiddlewareContent(this, Source.fromInputStream(stream).mkString)
  }

  override def getName: String = NAME
}
