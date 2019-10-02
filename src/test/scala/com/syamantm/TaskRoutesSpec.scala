package com.syamantm

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ ForAllTestContainer, PostgreSQLContainer }
import com.syamantm.db.{ FlywaySupport, TaskRepository }
import com.syamantm.util.DatabaseProvider
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterEach, Matchers, WordSpec }
import slick.basic.DatabaseConfig

import scala.collection.JavaConverters._

case class TestDatabaseProvider(container: PostgreSQLContainer) extends DatabaseProvider {
  override protected def dbConfig = {
    val testConfig = Map(
      "db.url" -> container.jdbcUrl,
      "db.user" -> container.username,
      "db.password" -> container.password,
      "profile" -> "slick.jdbc.PostgresProfile$")
    DatabaseConfig.forConfig("", ConfigFactory.parseMap(testConfig.asJava))
  }
}

class TaskRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with TaskRoutes with ForAllTestContainer with BeforeAndAfterEach {

  override val container = PostgreSQLContainer(username = "postgres", password = "postgres", databaseName = "postgres")

  lazy val databaseProvider = TestDatabaseProvider(container)

  lazy val taskRepository = TaskRepository(databaseProvider.profile)
  lazy val taskService = TaskService(taskRepository, databaseProvider)
  lazy val flyway = FlywaySupport(container.jdbcUrl, container.username, container.password)

  // Here we need to implement all the abstract members of TaskRoutes.
  // We use the real TaskRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  override lazy val taskRegistryActor: ActorRef =
    system.actorOf(TaskRegistryActor.props(taskService), "testTaskRegistry")

  lazy val routes = taskRoutes

  override def beforeEach() {
    flyway.migrate()
  }

  override def afterEach(): Unit = {
    flyway.dropDatabase()
  }

  "TaskRoutes" should {
    "return no tasks if no present (GET /tasks)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/tasks")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"tasks":[]}""")
      }
    }

    "be able to add tasks (POST /tasks)" in {
      val task = Task("task1", "my test task")
      val taskEntity = Marshal(task).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/tasks").withEntity(taskEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        val responseEntity = entityAs[TaskResponse]
        responseEntity.description should ===("my test task")
      }
    }

    "be able to remove tasks (DELETE /tasks)" in {
      val task = Task("task1", "my test task")
      val taskEntity = Marshal(task).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/tasks").withEntity(taskEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val responseEntity = entityAs[TaskResponse]
        val requestDelete = Delete(uri = s"/tasks/${responseEntity.id}")

        requestDelete ~> routes ~> check {
          status should ===(StatusCodes.OK)
        }
      }
    }
  }

}
