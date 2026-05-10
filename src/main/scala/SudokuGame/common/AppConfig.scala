package SudokuGame.common

import com.typesafe.config.ConfigFactory

object AppConfig:
  private val config = ConfigFactory.load()
  val loginEndpoint    : String = config.getString("aws.api.login-endpoint")
  val registerEndpoint : String = config.getString("aws.api.register-endpoint")
  val recentGamesEndpoint : String = config.getString("aws.api.get-last-games")