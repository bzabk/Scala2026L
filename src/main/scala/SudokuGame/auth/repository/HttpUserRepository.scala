package SudokuGame.auth.repository

import SudokuGame.auth.domain.User
import SudokuGame.common.AwsClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.*
import com.typesafe.config.ConfigFactory

class HttpUserRepository extends UserRepository {

  private val client = HttpClient.newHttpClient()

  private val config = ConfigFactory.load()
  private val baseUrl = config.getString("aws.api.base-url")
  private val loginEndpoint = config.getString("aws.api.login-endpoint")
  private val registerEndpoint = config.getString("aws.api.register-endpoint")


  override def register(email: String,passwordHash: String): Future[Boolean] = {
    val requestBody = s"""{"username": "${email}", "password": "${passwordHash}"}"""
    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$registerEndpoint"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(response => {
        if (response.statusCode() == 200) {

          true
        } else {
          println(response.body())
          false
        }
      })
      .asScala
  }

  override def login(email: String, password: String): Future[Boolean] = {
    val requestBody = s"""{"email": "$email", "password": "$password"}"""
    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$loginEndpoint"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(response => {
        if (response.statusCode() == 200) {
          true
        } else {
          false
        }
      })
      .asScala
  }



}
