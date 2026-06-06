package SudokuGame.controller

import scala.collection.immutable.SortedSet

class SudokuGameControllerTest extends munit.FunSuite {
  private val puzzle = Array(
    Array(5, 3, 0, 0, 7, 0, 0, 0, 0),
    Array(6, 0, 0, 1, 9, 5, 0, 0, 0),
    Array(0, 9, 8, 0, 0, 0, 0, 6, 0),
    Array(8, 0, 0, 0, 6, 0, 0, 0, 3),
    Array(4, 0, 0, 8, 0, 3, 0, 0, 1),
    Array(7, 0, 0, 0, 2, 0, 0, 0, 6),
    Array(0, 6, 0, 0, 0, 0, 2, 8, 0),
    Array(0, 0, 0, 4, 1, 9, 0, 0, 5),
    Array(0, 0, 0, 0, 8, 0, 0, 7, 9)
  )

  private def puzzleCopy(): Array[Array[Int]] =
    puzzle.map(_.clone())

  private val solvedBoard = Seq(
    Seq(5, 3, 4, 6, 7, 8, 9, 1, 2),
    Seq(6, 7, 2, 1, 9, 5, 3, 4, 8),
    Seq(1, 9, 8, 3, 4, 2, 5, 6, 7),
    Seq(8, 5, 9, 7, 6, 1, 4, 2, 3),
    Seq(4, 2, 6, 8, 5, 3, 7, 9, 1),
    Seq(7, 1, 3, 9, 2, 4, 8, 5, 6),
    Seq(9, 6, 1, 5, 3, 7, 2, 8, 4),
    Seq(2, 8, 7, 4, 1, 9, 6, 3, 5),
    Seq(3, 4, 5, 2, 8, 6, 1, 7, 9)
  )

  test("hint fills selected editable cell and consumes one hint") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())
    controller.selectCell(0, 2)

    val revealed = controller.revealHint()

    assert(revealed)
    assertEquals(controller.gameState.board.getCellValue(0, 2), 4)
    assertEquals(controller.gameState.hintsRemaining, 2)
    assert(controller.gameState.canUndo)
  }

  test("hint does not change given cells") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())
    controller.selectCell(0, 0)

    val revealed = controller.revealHint()

    assert(revealed)
    assertEquals(controller.gameState.board.getCellValue(0, 0), 5)
    assertEquals(controller.gameState.hintsRemaining, 2)
  }

  test("hint is unavailable when no hints remain") {
    val controller = new SudokuGameController()
    controller.startSavedGame(
      initialBoard = puzzle.map(_.toSeq).toSeq,
      boardValues = puzzle.map(_.toSeq).toSeq,
      notes = Seq.fill(9, 9)(Seq.empty[Int]),
      elapsedSeconds = 12,
      errorCount = 0,
      hintsRemaining = 0
    )
    controller.selectCell(0, 2)

    val revealed = controller.revealHint()

    assert(!revealed)
    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
    assertEquals(controller.gameState.hintsRemaining, 0)
  }

  test("saved game restores board, notes, timer, errors and hints") {
    val boardValues = puzzle.map(_.toSeq).toSeq
      .updated(0, puzzle(0).toSeq.updated(2, 4))
    val notes = Seq.tabulate(9, 9) { (row, col) =>
      if row == 1 && col == 1 then Seq(2, 7) else Seq.empty[Int]
    }
    val controller = new SudokuGameController()

    controller.startSavedGame(
      initialBoard = puzzle.map(_.toSeq).toSeq,
      boardValues = boardValues,
      notes = notes,
      elapsedSeconds = 91,
      errorCount = 1,
      hintsRemaining = 2
    )

    assertEquals(controller.initialBoard.head.take(3), Seq(5, 3, 0))
    assertEquals(controller.gameState.board.getCellValue(0, 2), 4)
    assertEquals(controller.gameState.board.getCellNotes(1, 1), SortedSet(2, 7))
    assertEquals(controller.gameState.elapsedSeconds, 91)
    assertEquals(controller.gameState.errorCount, 1)
    assertEquals(controller.gameState.hintsRemaining, 2)
    assert(controller.gameState.board.isCellGiven(0, 0))
    assert(!controller.gameState.board.isCellGiven(0, 2))
  }

  test("hint without a selected cell fills the first editable mismatch") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())

    val revealed = controller.revealHint()

    assert(revealed)
    assertEquals(controller.gameState.selectedRow, Some(0))
    assertEquals(controller.gameState.selectedCol, Some(2))
    assertEquals(controller.gameState.board.getCellValue(0, 2), 4)
  }

  test("placeNumber can be undone and redone") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())
    controller.selectCell(0, 2)

    controller.placeNumber(4)
    assertEquals(controller.gameState.board.getCellValue(0, 2), 4)
    assert(controller.gameState.canUndo)

    controller.undo()
    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
    assert(controller.gameState.canRedo)

    controller.redo()
    assertEquals(controller.gameState.board.getCellValue(0, 2), 4)
  }

  test("restart restores the cloned initial board") {
    val initial = puzzleCopy()
    val controller = new SudokuGameController()
    controller.startNewGame(initial)
    initial(0)(2) = 9
    controller.selectCell(0, 2)
    controller.placeNumber(4)
    controller.updateTime()

    controller.restartGame()

    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
    assertEquals(controller.gameState.elapsedSeconds, 0)
    assertEquals(controller.gameState.hintsRemaining, 3)
  }

  test("placeNumber ignores missing selection, given cells and invalid values") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())

    controller.placeNumber(4)
    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)

    controller.selectCell(0, 0)
    controller.placeNumber(4)
    assertEquals(controller.gameState.board.getCellValue(0, 0), 5)

    controller.selectCell(0, 2)
    controller.placeNumber(10)
    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
  }

  test("notes mode records notes and clearCell removes them") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())
    controller.selectCell(0, 2)

    controller.toggleNotesMode()
    controller.placeNumber(4)
    controller.placeNumber(6)

    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
    assertEquals(controller.gameState.board.getCellNotes(0, 2), SortedSet(4, 6))
    assertEquals(controller.gameState.errorCount, 0)

    controller.clearCell()

    assertEquals(controller.gameState.board.getCellNotes(0, 2), SortedSet.empty[Int])
    assert(controller.gameState.canUndo)
  }

  test("moveSelection clamps coordinates to board bounds") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())

    controller.moveSelection(-5, -5)
    assertEquals(controller.gameState.selectedRow, Some(0))
    assertEquals(controller.gameState.selectedCol, Some(0))

    controller.moveSelection(20, 20)
    assertEquals(controller.gameState.selectedRow, Some(8))
    assertEquals(controller.gameState.selectedCol, Some(8))
  }

  test("pause blocks timer, selection changes and placements") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())
    controller.selectCell(0, 2)
    controller.togglePause()

    controller.updateTime()
    controller.selectCell(1, 1)
    controller.placeNumber(4)

    assertEquals(controller.gameState.elapsedSeconds, 0)
    assertEquals(controller.gameState.selectedRow, Some(0))
    assertEquals(controller.gameState.selectedCol, Some(2))
    assertEquals(controller.gameState.board.getCellValue(0, 2), 0)
  }

  test("conflicting placements increase errors and end the game at the limit") {
    val controller = new SudokuGameController()
    controller.startNewGame(puzzleCopy())

    Seq(2, 3, 5).foreach { col =>
      controller.selectCell(0, col)
      controller.placeNumber(5)
    }

    assertEquals(controller.gameState.errorCount, 3)
    assert(controller.gameState.isGameOver)
  }

  test("placing the final correct number marks the game as over") {
    val initialBoard = solvedBoard.updated(8, solvedBoard(8).updated(8, 0))
    val controller = new SudokuGameController()
    controller.startSavedGame(
      initialBoard = initialBoard,
      boardValues = initialBoard,
      notes = Seq.fill(9, 9)(Seq.empty[Int]),
      elapsedSeconds = 10,
      errorCount = 0,
      hintsRemaining = 3
    )

    controller.selectCell(8, 8)
    controller.placeNumber(9)

    assert(controller.gameState.isGameOver)
    assert(controller.gameState.isSolved)
  }
}
