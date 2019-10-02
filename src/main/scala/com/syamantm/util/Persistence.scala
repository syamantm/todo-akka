package com.syamantm.util

import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
 * @author syamantak.
 */
trait Profile {
  val profile: JdbcProfile
}

trait DbModule extends Profile {
  val db: JdbcProfile#Backend#Database

  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T] = {
    db.run(databaseOperation)
  }

}

trait PersistenceModule {
  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T]
}

trait DatabaseProvider extends PersistenceModule with DbModule {
  protected def dbConfig: DatabaseConfig[JdbcProfile]
  override implicit val profile: JdbcProfile = dbConfig.profile
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db
}

class Persistence() extends DatabaseProvider {
  override def dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("database")
}