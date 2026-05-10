package SudokuGame.game.repository

import SudokuGame.common.{AppConfig, GameState, GameStatus, HttpClientProvider}

import java.net.URI
import java.net.http.{HttpRequest, HttpResponse}
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

class GameRepository {

  private val client         = HttpClientProvider.client
  private val recentEndpoint = AppConfig.recentGamesEndpoint

  def fetchRecentGames(username: String): Future[List[GameState]] = {
    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$recentEndpoint?username=$username"))
      .GET()
      .build()
    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply { response =>
        if response.statusCode() == 200 then parseGames(response.body())
        else List.empty
      }
      .asScala
  }

  private def parseGames(body: String): List[GameState] =
    ujson.read(body).arr.map { item =>
      val board = ujson.read(item("board").str).arr
        .map(_.arr.map(_.num.toInt).toSeq).toSeq
      val status =if item("status").str == "in_progress" then GameStatus.InProgress else GameStatus.Finished
      GameState(
        createdAt      = item("createdAt").str,
        difficulty     = item("difficulty").str,
        elapsedSeconds = item("elapsedSeconds").num.toLong,
        board          = board,
        status         = status
      )
    }.toList
}
