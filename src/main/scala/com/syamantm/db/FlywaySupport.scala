package com.syamantm.db

import org.flywaydb.core.Flyway

/**
 * @author syamantak.
 */
case class FlywaySupport(jdbcUrl: String, dbUser: String, dbPassword: String) {

  private[this] val flyway = Flyway.configure().dataSource(jdbcUrl, dbUser, dbPassword).load()

  def migrate(): Unit = flyway.migrate()

  def dropDatabase(): Unit = flyway.clean()
}
