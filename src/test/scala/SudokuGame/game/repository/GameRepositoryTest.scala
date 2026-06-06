package SudokuGame.game.repository

import SudokuGame.common.GameStatus

class GameRepositoryTest extends munit.FunSuite {
  private val validBoard = ujson.Arr(
    Seq.fill(9)(ujson.Arr(Seq.fill(9)(ujson.Num(0))*))*
  )
  private val validNotes = ujson.Arr(
    Seq.fill(9)(ujson.Arr(Seq.fill(9)(ujson.Arr())*))*
  )

  test("parseGames skips malformed items instead of dropping the whole response") {
    val body = ujson.write(
      ujson.Arr(
        ujson.Obj(
          "createdAt" -> "2026-06-06T10:00:00Z",
          "difficulty" -> "Hard",
          "elapsedSeconds" -> 125,
          "status" -> "in_progress",
          "gameId" -> "game-1",
          "board" -> validBoard
        ),
        ujson.Obj(
          "createdAt" -> "broken",
          "difficulty" -> "Easy",
          "board" -> ujson.Str("this is not json")
        )
      )
    )

    val games = new GameRepository().parseGames(body)

    assertEquals(games.size, 1)
    assertEquals(games.head.gameId, "game-1")
    assertEquals(games.head.difficulty, "Hard")
    assertEquals(games.head.status, GameStatus.InProgress)
  }

  test("parseGames accepts games wrapper and string encoded matrix fields") {
    val initialBoard = ujson.Arr(
      Seq.tabulate(9) { row =>
        ujson.Arr(Seq.tabulate(9)(col => ujson.Num(if row == col then 1 else 0))*)
      }*
    )
    val notes = ujson.Arr(
      Seq.tabulate(9) { row =>
        ujson.Arr(
          Seq.tabulate(9) { col =>
            if row == 2 && col == 3 then ujson.Arr(ujson.Num(4), ujson.Num(8))
            else ujson.Arr()
          }*
        )
      }*
    )
    val body = ujson.write(
      ujson.Obj(
        "games" -> ujson.Arr(
          ujson.Obj(
            "createdAt" -> "2026-06-06T12:00:00Z",
            "difficulty" -> "Expert",
            "elapsedSeconds" -> "240",
            "status" -> "finished",
            "id" -> 123,
            "board" -> ujson.write(validBoard),
            "initial_board" -> ujson.write(initialBoard),
            "notes" -> ujson.write(notes),
            "errorCount" -> "2",
            "hintsRemaining" -> "1"
          )
        )
      )
    )

    val games = new GameRepository().parseGames(body)

    assertEquals(games.size, 1)
    assertEquals(games.head.gameId, "123")
    assertEquals(games.head.elapsedSeconds, 240L)
    assertEquals(games.head.status, GameStatus.Finished)
    assertEquals(games.head.initialBoard(0)(0), 1)
    assertEquals(games.head.notes(2)(3), Seq(4, 8))
    assertEquals(games.head.errorCount, 2)
    assertEquals(games.head.hintsRemaining, 1)
  }

  test("parseGames accepts data wrapper and fills optional save fields") {
    val body = ujson.write(
      ujson.Obj(
        "data" -> ujson.Arr(
          ujson.Obj(
            "createdAt" -> "2026-06-06T13:00:00Z",
            "difficulty" -> "Easy",
            "elapsedSeconds" -> 15,
            "board" -> validBoard,
            "notes" -> validNotes
          )
        )
      )
    )

    val games = new GameRepository().parseGames(body)

    assertEquals(games.size, 1)
    assertEquals(games.head.status, GameStatus.Finished)
    assertEquals(games.head.gameId, "")
    assertEquals(games.head.errorCount, 0)
    assertEquals(games.head.hintsRemaining, 3)
  }

  test("parseGames returns an empty list when response does not contain games") {
    val body = ujson.write(ujson.Obj("message" -> "No games yet"))

    val games = new GameRepository().parseGames(body)

    assertEquals(games, List.empty)
  }

  test("parseGames treats uppercase in-progress status as in progress") {
    val body = ujson.write(
      ujson.Arr(
        ujson.Obj(
          "createdAt" -> "2026-06-06T14:00:00Z",
          "difficulty" -> "Medium",
          "elapsedSeconds" -> 30,
          "status" -> "IN_PROGRESS",
          "board" -> validBoard
        )
      )
    )

    val games = new GameRepository().parseGames(body)

    assertEquals(games.size, 1)
    assertEquals(games.head.status, GameStatus.InProgress)
  }

  test("parseGames skips entries without a readable board") {
    val body = ujson.write(
      ujson.Arr(
        ujson.Obj(
          "createdAt" -> "2026-06-06T15:00:00Z",
          "difficulty" -> "Easy",
          "elapsedSeconds" -> 10,
          "status" -> "finished"
        )
      )
    )

    val games = new GameRepository().parseGames(body)

    assertEquals(games, List.empty)
  }
}
