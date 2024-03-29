package com.syamantm

import com.syamantm.TaskRegistryActor.ActionPerformed

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait TaskJsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val taskJsonFormat = jsonFormat2(Task)
  implicit val taskResponseJsonFormat = jsonFormat3(TaskResponse)
  implicit val tasksJsonFormat = jsonFormat1(Tasks)
  implicit val deletedResponseJsonFormat = jsonFormat1(DeleteResponse)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
