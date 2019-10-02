package com.syamantm.db

import com.byteslounge.slickrepo.meta.{ Entity, Keyed }
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

/**
 * @author syamantak.
 */

case class TaskEntity(
  override val id: Option[Int],
  title: String,
  description: String) extends Entity[TaskEntity, Int] {
  def withId(id: Int): TaskEntity = this.copy(id = Some(id))
}

case class TaskRepository(override val driver: JdbcProfile) extends Repository[TaskEntity, Int](driver) {

  import driver.api._
  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Tasks]
  type TableType = Tasks

  class Tasks(tag: slick.lifted.Tag) extends Table[TaskEntity](tag, "task") with Keyed[Int] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")

    def * = (id.?, title, description) <> ((TaskEntity.apply _).tupled, TaskEntity.unapply)
  }
}
