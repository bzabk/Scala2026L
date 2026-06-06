package SudokuGame.common

class GameStateTest extends munit.FunSuite {
  test("resumeInitialBoard falls back to board for older backend payloads") {
    val board = Seq.fill(9, 9)(0)
    val game = GameState(
      createdAt = "2026-06-05T12:00:00Z",
      difficulty = "Easy",
      elapsedSeconds = 10,
      board = board,
      status = GameStatus.InProgress
    )

    assertEquals(game.resumeInitialBoard, board)
  }

  test("normalizedNotes returns an empty notes grid when backend omits notes") {
    val game = GameState(
      createdAt = "2026-06-05T12:00:00Z",
      difficulty = "Easy",
      elapsedSeconds = 10,
      board = Seq.fill(9, 9)(0),
      status = GameStatus.InProgress
    )

    assertEquals(game.normalizedNotes.size, 9)
    assertEquals(game.normalizedNotes.head.size, 9)
    assert(game.normalizedNotes.flatten.forall(_.isEmpty))
  }

  test("resumeInitialBoard prefers backend initial board when present") {
    val currentBoard = Seq.fill(9, 9)(0)
    val initialBoard = Seq.tabulate(9, 9) { (row, col) =>
      if row == 0 && col == 0 then 5 else 0
    }
    val game = GameState(
      createdAt = "2026-06-05T12:00:00Z",
      difficulty = "Medium",
      elapsedSeconds = 42,
      board = currentBoard,
      status = GameStatus.InProgress,
      initialBoard = initialBoard
    )

    assertEquals(game.resumeInitialBoard, initialBoard)
  }

  test("normalizedNotes preserves backend notes when present") {
    val notes = Seq.tabulate(9, 9) { (row, col) =>
      if row == 3 && col == 4 then Seq(2, 6) else Seq.empty[Int]
    }
    val game = GameState(
      createdAt = "2026-06-05T12:00:00Z",
      difficulty = "Hard",
      elapsedSeconds = 80,
      board = Seq.fill(9, 9)(0),
      status = GameStatus.InProgress,
      notes = notes
    )

    assertEquals(game.normalizedNotes, notes)
  }
}
