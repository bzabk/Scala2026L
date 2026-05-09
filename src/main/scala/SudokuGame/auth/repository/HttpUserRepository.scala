package SudokuGame.auth.repository

import SudokuGame.common.{AppConfig, HttpClientProvider}

import java.net.URI
import java.net.http.{HttpRequest, HttpResponse}
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

class HttpUserRepository extends UserRepository {

  private val client           = HttpClientProvider.client
  private val loginEndpoint    = AppConfig.loginEndpoint
  private val registerEndpoint = AppConfig.registerEndpoint

  override def register(email: String, username: String, passwordHash: String): Future[Boolean] = {
    val body = s"""{"email": "$email", "username": "$username", "password": "$passwordHash"}"""
    val request = HttpRequest.newBuilder()
      .uri(URI.create(registerEndpoint))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build()

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(_.statusCode() == 200)
      .asScala
  }

  override def login(username: String, password: String): Future[Boolean] = {
    val body = s"""{"username": "$username", "password": "$password"}"""
    val request = HttpRequest.newBuilder()
      .uri(URI.create(loginEndpoint))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build()

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(_.statusCode() == 200)
      .asScala
  }
}