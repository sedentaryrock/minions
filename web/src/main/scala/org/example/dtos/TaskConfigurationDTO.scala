package org.example.dtos

import scala.concurrent.duration.FiniteDuration
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import java.util.Base64

import org.example.model.TaskConfiguration

case class TaskConfigurationDTO(
  startupDelay: FiniteDuration,
  pollDuration: FiniteDuration,
  topic: String,
  fromStatus: String,
  toStatus: String,
  taskClass: String,
  task: Array[Byte]
)

object TaskConfigurationDTO {
  implicit val finiteDurationDecoder: Decoder[FiniteDuration] = Decoder.instance { cursor =>
    cursor.as[Long].map(millis => FiniteDuration(millis, scala.concurrent.duration.MILLISECONDS))
  }

  implicit val finiteDurationEncoder: Encoder[FiniteDuration] = 
    Encoder[Long].contramap(_.toMillis)

  implicit val byteArrayDecoder: Decoder[Array[Byte]] = 
    Decoder[String].emapTry { base64Str =>
      scala.util.Try(Base64.getDecoder.decode(base64Str))
    }

  implicit val byteArrayEncoder: Encoder[Array[Byte]] = 
    Encoder[String].contramap(bytes => Base64.getEncoder.encodeToString(bytes))

  implicit val decoder: Decoder[TaskConfigurationDTO] = deriveDecoder
  implicit val encoder: Encoder[TaskConfigurationDTO] = deriveEncoder

  def fromTaskConfiguration(config: TaskConfiguration): TaskConfigurationDTO = {
    TaskConfigurationDTO(
      startupDelay = config.startupDelay,
      pollDuration = config.pollDuration,
      topic = config.topic,
      fromStatus = config.fromStatus,
      toStatus = config.toStatus,
      taskClass = config.taskClass.getOrElse(""),
      task = config.task.getOrElse(Array.emptyByteArray)
    )
  }
}