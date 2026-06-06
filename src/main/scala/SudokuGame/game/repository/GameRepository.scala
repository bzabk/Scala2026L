package SudokuGame.game.repository

import SudokuGame.common.{AppConfig, GameState, GameStatus, HttpClientProvider}

import java.net.URI
import java.net.URLEncoder
import java.net.http.{HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import scala.concurrent.Future
import scala.jdk.FutureConverters.*
import scala.util.Try

class GameRepository {

  private val client         = HttpClientProvider.client
  private val recentEndpoint = AppConfig.recentGamesEndpoint
  private val saveEndpoint   = AppConfig.saveGameEndpoint

  def fetchRecentGames(username: String): Future[List[GameState]] = {
    val encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8)
    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$recentEndpoint?username=$encodedUsername"))
      .GET()
      .build()
    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply { response =>
        if response.statusCode() == 200 then
          Try(parseGames(response.body())).getOrElse(List.empty)
        else List.empty
      }
      .asScala
  }

  def saveGame(username: String, game: GameState): Future[Boolean] = {
    val request = HttpRequest.newBuilder()
      .uri(URI.create(saveEndpoint))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(gameToJson(username, game)))
      .build()

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(response =>
        response.statusCode() == 200 ||
          response.statusCode() == 201 ||
          response.statusCode() == 204
      )
      .asScala
  }

  private[repository] def parseGames(body: String): List[GameState] =
    gamesArray(ujson.read(body)).flatMap(parseGame).toList

  private def parseGame(item: ujson.Value): Option[GameState] =
    Try {
      val board = readIntMatrix(item("board"))
      val initialBoard = item.obj
        .get("initialBoard")
        .orElse(item.obj.get("initial_board"))
        .map(readIntMatrix)
        .getOrElse(Seq.empty)
      val notes = item.obj
        .get("notes")
        .map(readNotesMatrix)
        .getOrElse(Seq.empty)
      val status = item.obj
        .get("status")
        .flatMap {
          case value: ujson.Str => Some(value.value)
          case _                => None
        }
        .map(_.toLowerCase)
        .filter(_ == "in_progress")
        .fold(GameStatus.Finished)(_ => GameStatus.InProgress)

      GameState(
        createdAt      = readString(item, "createdAt").getOrElse(""),
        difficulty     = readString(item, "difficulty").getOrElse("Easy"),
        elapsedSeconds = readLong(item, "elapsedSeconds").getOrElse(0L),
        board          = board,
        status         = status,
        gameId = readString(item, "gameId")
          .orElse(readString(item, "id"))
          .getOrElse(""),
        initialBoard   = initialBoard,
        notes          = notes,
        errorCount     = readLong(item, "errorCount").map(_.toInt).getOrElse(0),
        hintsRemaining = readLong(item, "hintsRemaining").map(_.toInt).getOrElse(3)
      )
    }.toOption

  private def gamesArray(value: ujson.Value): Seq[ujson.Value] =
    value match {
      case arr: ujson.Arr => arr.value.toSeq
      case obj: ujson.Obj =>
        obj.obj.get("games").collect { case arr: ujson.Arr => arr.value.toSeq }
          .orElse(obj.obj.get("data").collect { case arr: ujson.Arr => arr.value.toSeq })
          .getOrElse(Seq.empty)
      case _ => Seq.empty
    }

  private def readString(item: ujson.Value, key: String): Option[String] =
    item.obj.get(key).flatMap {
      case value: ujson.Str => Some(value.value)
      case value: ujson.Num => Some(value.value.toLong.toString)
      case _                => None
    }

  private def readLong(item: ujson.Value, key: String): Option[Long] =
    item.obj.get(key).flatMap {
      case value: ujson.Num => Some(value.value.toLong)
      case value: ujson.Str => value.value.toLongOption
      case _                => None
    }

  private def readIntMatrix(value: ujson.Value): Seq[Seq[Int]] =
    readArrayValue(value).arr.map { row =>
      row.arr.map(_.num.toInt).toSeq
    }.toSeq

  private def readNotesMatrix(value: ujson.Value): Seq[Seq[Seq[Int]]] =
    readArrayValue(value).arr.map { row =>
      row.arr.map(cell => cell.arr.map(_.num.toInt).toSeq).toSeq
    }.toSeq

  private def readArrayValue(value: ujson.Value): ujson.Value =
    value match {
      case str: ujson.Str => ujson.read(str.value)
      case other          => other
    }

  private def gameToJson(username: String, game: GameState): String =
    ujson.write(
      ujson.Obj(
        "username" -> username,
        "gameId" -> game.gameId,
        "createdAt" -> game.createdAt,
        "difficulty" -> game.difficulty,
        "elapsedSeconds" -> game.elapsedSeconds,
        "status" -> statusToJson(game.status),
        "board" -> matrixToJson(game.board),
        "initialBoard" -> matrixToJson(game.resumeInitialBoard),
        "notes" -> notesToJson(game.normalizedNotes),
        "errorCount" -> game.errorCount,
        "hintsRemaining" -> game.hintsRemaining
      )
    )

  private def statusToJson(status: GameStatus): String =
    status match {
      case GameStatus.InProgress => "in_progress"
      case GameStatus.Finished   => "finished"
    }

  private def matrixToJson(matrix: Seq[Seq[Int]]): ujson.Value =
    ujson.Arr(matrix.map(row => ujson.Arr(row.map(value => ujson.Num(value))*))*)

  private def notesToJson(notes: Seq[Seq[Seq[Int]]]): ujson.Value =
    ujson.Arr(
      notes.map(row =>
        ujson.Arr(row.map(cell => ujson.Arr(cell.map(value => ujson.Num(value))*))*)
      )*
    )
}
