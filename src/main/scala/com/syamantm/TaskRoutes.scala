package com.syamantm

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.syamantm.TaskRegistryActor._

import scala.concurrent.Future
import scala.concurrent.duration._

trait TaskRoutes extends TaskJsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[TaskRoutes])

  // other dependencies that TaskRoutes use
  def taskRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val taskRoutes: Route =
    pathPrefix("tasks") {
      concat(
        pathEnd {
          concat(
            get {
              val tasks: Future[Tasks] =
                (taskRegistryActor ? GetTasks).mapTo[Tasks]
              complete((StatusCodes.OK, tasks))
            },
            post {
              entity(as[Task]) { task =>
                val taskCreated: Future[TaskResponse] =
                  (taskRegistryActor ? CreateTask(task)).mapTo[TaskResponse]
                onSuccess(taskCreated) { performed =>
                  log.info("Created task [{}]: {}", task.title, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { id =>
          concat(
            get {
              val maybeTask: Future[Option[TaskResponse]] =
                (taskRegistryActor ? GetTask(id.toInt)).mapTo[Option[TaskResponse]]
              rejectEmptyResponse {
                complete(maybeTask)
              }
            },
            delete {
              val taskDeleted: Future[Option[String]] =
                (taskRegistryActor ? DeleteTask(id.toInt)).mapTo[Option[String]]
              onSuccess(taskDeleted) { deleted =>
                deleted.fold(complete((StatusCodes.NotFound, s"Task with id {$id} not found"))) { msg =>
                  log.info("Deleted task [{}]: {}", id, msg)
                  complete((StatusCodes.OK, msg))
                }

              }
            })
        })
    }
}
