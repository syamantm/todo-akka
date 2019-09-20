package com.syamantm

import akka.actor.{ Actor, ActorLogging, Props }

//#task-case-classes
final case class Task(title: String, description: String)
final case class Tasks(tasks: Seq[Task])
//#task-case-classes

object TaskRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetTasks
  final case class CreateTask(task: Task)
  final case class GetTask(title: String)
  final case class DeleteTask(title: String)

  def props: Props = Props[TaskRegistryActor]
}

class TaskRegistryActor extends Actor with ActorLogging {
  import TaskRegistryActor._

  var tasks = Set.empty[Task]

  def receive: Receive = {
    case GetTasks =>
      sender() ! Tasks(tasks.toSeq)
    case CreateTask(task) =>
      tasks += task
      sender() ! ActionPerformed(s"Task ${task.title} created.")
    case GetTask(title) =>
      sender() ! tasks.find(_.title == title)
    case DeleteTask(title) =>
      tasks.find(_.title == title) foreach { task => tasks -= task }
      sender() ! ActionPerformed(s"Task $title deleted.")
  }
}