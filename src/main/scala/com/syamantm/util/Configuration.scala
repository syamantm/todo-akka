package com.syamantm.util

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * @author syamantak.
 */
trait Configuration {
  protected val config: Config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("database")

  val httpHost = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")
  val httpSelfTimeout = httpConfig.getDuration("self-timeout")

  val jdbcUrl = databaseConfig.getString("db.url")
  val dbUser = databaseConfig.getString("db.user")
  val dbPassword = databaseConfig.getString("db.password")
}
