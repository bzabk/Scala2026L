package SudokuGame.auth.repository

import com.typesafe.config.ConfigFactory

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

class HttpUserRepository extends UserRepository {

  private val client = HttpClient.newHttpClient()

  private val config           = ConfigFactory.load()
  private val loginEndpoint    = config.getString("aws.api.login-endpoint")
  private val registerEndpoint = config.getString("aws.api.register-endpoint")

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