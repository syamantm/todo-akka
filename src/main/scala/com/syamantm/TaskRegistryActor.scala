package com.syamantm

import java.util.concurrent.Executors

import akka.actor.{ Actor, ActorLogging, Props }
import akka.util.Timeout
import com.syamantm.db.{ TaskEntity, TaskRepository }
import com.syamantm.util.DatabaseProvider

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

final case class Task(title: String, description: String)

final case class TaskResponse(id: Int, title: String, description: String)

final case class DeleteResponse(deleted: Boolean)

final case class Tasks(tasks: Seq[TaskResponse])

object TaskRegistryActor {

  final case class ActionPerformed(description: String)

  final case object GetTasks

  final case class CreateTask(task: Task)

  final case class GetTask(id: Int)

  final case class DeleteTask(id: Int)

  def props(taskService: TaskService): Props = Props(new TaskRegistryActor(taskService))
}

class TaskRegistryActor(val taskService: TaskService) extends Actor with ActorLogging {

  import TaskRegistryActor._
  import akka.pattern.pipe

  implicit val timeout: Timeout = 5.seconds
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  def receive: Receive = {
    case GetTasks =>
      taskService
        .getTasks
        .pipeTo(sender())

    case CreateTask(task) =>
      taskService
        .addTask(task)
        .pipeTo(sender())

    case GetTask(id) =>
      taskService
        .getTask(id)
        .pipeTo(sender())

    case DeleteTask(id) =>
      taskService
        .deleteTask(id)
        .pipeTo(sender())
  }
}

case class TaskService(
  taskRepository: TaskRepository,
  databaseProvider: DatabaseProvider) {

  import databaseProvider._

  def getTasks(implicit executionContext: ExecutionContext): Future[Tasks] = {
    executeOperation(taskRepository.findAll())
      .map(taskEntities =>
        taskEntities
          .map(e => TaskResponse(e.id.getOrElse(0), e.title, e.description)))
      .map(tasks => Tasks(tasks))
  }

  def addTask(task: Task)(implicit executionContext: ExecutionContext): Future[TaskResponse] = {
    executeOperation(taskRepository.save(TaskEntity(None, task.title, task.description))
      .map(e => TaskResponse(e.id.getOrElse(0), e.title, e.description)))

  }

  def getTask(id: Int)(implicit executionContext: ExecutionContext): Future[Option[TaskResponse]] = {
    executeOperation(taskRepository.findOne(id)
      .map(optEntity => optEntity.map(e => TaskResponse(e.id.getOrElse(0), e.title, e.description))))

  }

  def deleteTask(id: Int)(implicit executionContext: ExecutionContext): Future[Option[String]] = {
    executeOperation(taskRepository.findOne(id))
      .flatMap(deleteIfExists)
  }

  private def deleteIfExists(optEntity: Option[TaskEntity])(implicit executionContext: ExecutionContext): Future[Option[String]] = {
    optEntity.fold[Future[Option[String]]](Future.successful(None)) { entity =>
      executeOperation(taskRepository.delete(entity)).map(_ => Some("Deleted"))
    }
  }
}
