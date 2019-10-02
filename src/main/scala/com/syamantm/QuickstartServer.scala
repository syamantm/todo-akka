package com.syamantm

//#quick-start-server
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.syamantm.db.{ FlywaySupport, TaskRepository }
import com.syamantm.util.{ Configuration, Persistence }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

//#main-class
object QuickstartServer extends App with TaskRoutes with Configuration {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("todoAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  //#server-bootstrapping

  val persistence = new Persistence()
  val taskRepository = TaskRepository(persistence.profile)
  val taskService = TaskService(taskRepository, persistence)
  val flyway = FlywaySupport(jdbcUrl, dbUser, dbPassword)

  val taskRegistryActor: ActorRef = system.actorOf(TaskRegistryActor.props(taskService), "taskRegistryActor")

  //#main-class
  // from the TaskRoutes trait
  lazy val routes: Route = taskRoutes
  //#main-class

  // run database migration

  flyway.migrate()

  //#http-server
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      log.debug(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      log.error(e, "Server could not start!")
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
