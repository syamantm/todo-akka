package com.syamantm

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

//#set-up
class TaskRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with TaskRoutes {
  //#test-top

  // Here we need to implement all the abstract members of TaskRoutes.
  // We use the real TaskRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  override val taskRegistryActor: ActorRef =
    system.actorOf(TaskRegistryActor.props, "taskRegistry")

  lazy val routes = taskRoutes

  //#set-up

  //#actual-test
  "TaskRoutes" should {
    "return no tasks if no present (GET /tasks)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/tasks")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"tasks":[]}""")
      }
    }
    //#actual-test

    //#testing-post
    "be able to add tasks (POST /tasks)" in {
      val task = Task("task1", "my test task")
      val taskEntity = Marshal(task).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/tasks").withEntity(taskEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"Task task1 created."}""")
      }
    }
    //#testing-post

    "be able to remove tasks (DELETE /tasks)" in {
      // user the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/tasks/task1")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"Task task1 deleted."}""")
      }
    }
    //#actual-test
  }
  //#actual-test

  //#set-up
}
//#set-up
