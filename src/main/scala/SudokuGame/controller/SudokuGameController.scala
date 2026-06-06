package SudokuGame.controller

import SudokuGame.model.{BoardMove, GameState, SudokuBoard, SudokuSolver}
import scalafx.beans.property.ObjectProperty
import scala.collection.immutable

class SudokuGameController {
  private val _gameStateProperty =
    new ObjectProperty[GameState](this, "gameState", null)
  private var _initialBoard: Array[Array[Int]] = Array.empty

  private def _cloneBoard(board: Array[Array[Int]]): Array[Array[Int]] =
    board.map(_.clone())

  private def _toArrayBoard(board: Seq[Seq[Int]]): Array[Array[Int]] =
    Array.tabulate(9, 9) { (row, col) =>
      board.lift(row).flatMap(_.lift(col)).getOrElse(0)
    }

  def gameState: GameState = _gameStateProperty.value
  def gameState_=(state: GameState): Unit = _gameStateProperty.value = state

  def gameStateProperty: ObjectProperty[GameState] = _gameStateProperty

  def initialBoard: Seq[Seq[Int]] =
    if _initialBoard.isEmpty then Seq.fill(9, 9)(0)
    else _initialBoard.map(_.toSeq).toSeq

  def startNewGame(initialBoard: Array[Array[Int]]): Unit = {
    _initialBoard = _cloneBoard(initialBoard)
    val board = new SudokuBoard(_cloneBoard(initialBoard))
    gameState = GameState(board = board)
  }

  def startSavedGame(
      initialBoard: Seq[Seq[Int]],
      boardValues: Seq[Seq[Int]],
      notes: Seq[Seq[Seq[Int]]],
      elapsedSeconds: Int,
      errorCount: Int,
      hintsRemaining: Int
  ): Unit = {
    _initialBoard = _toArrayBoard(initialBoard)
    val board = new SudokuBoard(_cloneBoard(_initialBoard))
    board.loadSnapshot(boardValues, notes)
    gameState = GameState(
      board = board,
      elapsedSeconds = elapsedSeconds,
      errorCount = errorCount,
      hintsRemaining = hintsRemaining
    )
  }

  def restartGame(): Unit = {
    if (_initialBoard.isEmpty) return

    startNewGame(_cloneBoard(_initialBoard))
  }

  def clearGameState(): Unit = {
    gameState = null
  }

  def placeNumber(value: Int): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    if (gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty) return

    if (value < 1 || value > 9) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (gameState.board.isCellGiven(row, col)) return

    val previousValue = gameState.board.getCellValue(row, col)
    val updatedValue =
      if (!gameState.isNotesMode && value == previousValue) 0 else value
    val previousConflicts = gameState.conflicts(row)(col).toSet
    val previousNotes = gameState.board.getCellNotes(row, col)

    gameState.board.updateCell(row, col, updatedValue, gameState.isNotesMode)
    val currentValue = gameState.board.getCellValue(row, col)
    val currentNotes = gameState.board.getCellNotes(row, col)
    gameState = gameState.recordMove(
      BoardMove(
        row,
        col,
        previousValue,
        previousNotes,
        currentValue,
        currentNotes
      )
    )

    if (
      !gameState.isNotesMode && currentValue != 0 && gameState
        .conflicts(row)(col)
        .toSet != previousConflicts
    ) {
      gameState = gameState.copy(
        errorCount = gameState.errorCount + 1
      )
    }

    if (gameState.isSolved || gameState.isLost) {
      gameState = gameState.copy(isGameOver = true)
    }

    gameStateProperty.value = gameState
  }

  def clearCell(): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    if (gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (gameState.board.isCellGiven(row, col)) return

    val previousValue = gameState.board.getCellValue(row, col)
    val previousNotes = gameState.board.getCellNotes(row, col)

    if (previousValue == 0 && previousNotes.isEmpty) return

    gameState.board.updateCell(row, col, 0, false)
    val currentValue = gameState.board.getCellValue(row, col)
    val currentNotes = gameState.board.getCellNotes(row, col)
    gameState = gameState.recordMove(
      BoardMove(
        row,
        col,
        previousValue,
        previousNotes,
        currentValue,
        currentNotes
      )
    )

    gameStateProperty.value = gameState
  }

  def revealHint(): Boolean = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return false
    if (gameState.hintsRemaining <= 0) return false

    val solutionOpt = SudokuSolver.solve(initialBoard)
    solutionOpt match {
      case None => false
      case Some(solution) =>
        val targetOpt = hintTarget(solution)
        targetOpt match {
          case None => false
          case Some((row, col, value)) =>
            val previousValue = gameState.board.getCellValue(row, col)
            val previousNotes = gameState.board.getCellNotes(row, col)

            gameState.board.updateCell(row, col, value, false)
            gameState = gameState
              .recordMove(
                BoardMove(
                  row,
                  col,
                  previousValue,
                  previousNotes,
                  value,
                  immutable.SortedSet.empty
                )
              )
              .selectCell(row, col)
              .copy(hintsRemaining = gameState.hintsRemaining - 1)

            if (gameState.isSolved || gameState.isLost) {
              gameState = gameState.copy(isGameOver = true)
            }

            gameStateProperty.value = gameState
            true
        }
    }
  }

  private def hintTarget(solution: Seq[Seq[Int]]): Option[(Int, Int, Int)] = {
    val selectedTarget = for {
      row <- gameState.selectedRow
      col <- gameState.selectedCol
      if !gameState.board.isCellGiven(row, col)
      solutionValue <- solution.lift(row).flatMap(_.lift(col))
      if gameState.board.getCellValue(row, col) != solutionValue
    } yield (row, col, solutionValue)

    selectedTarget.orElse {
      (for {
        row <- 0 until 9
        col <- 0 until 9
        if !gameState.board.isCellGiven(row, col)
        solutionValue <- solution.lift(row).flatMap(_.lift(col))
        if gameState.board.getCellValue(row, col) != solutionValue
      } yield (row, col, solutionValue)).headOption
    }
  }

  def undo(): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    val (updatedState, moveOpt) = gameState.popUndoMove()
    moveOpt match {
      case Some(move) =>
        gameState.board.updateCell(
          move.row,
          move.col,
          move.previousValue,
          move.previousNotes
        )
      case None => ()
    }

    gameState = updatedState
    gameStateProperty.value = gameState
  }

  def redo(): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    val (updatedState, moveOpt) = gameState.popRedoMove()
    moveOpt match {
      case Some(move) =>
        gameState.board.updateCell(
          move.row,
          move.col,
          move.newValue,
          move.newNotes
        )
      case None => ()
    }

    gameState = updatedState
    gameStateProperty.value = gameState
  }

  def selectCell(row: Int, col: Int): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    gameState = gameState.selectCell(row, col)
    gameStateProperty.value = gameState
  }

  def moveSelection(deltaRow: Int, deltaCol: Int): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    val currentRow = gameState.selectedRow.getOrElse(0)
    val currentCol = gameState.selectedCol.getOrElse(0)
    val nextRow = (currentRow + deltaRow).max(0).min(8)
    val nextCol = (currentCol + deltaCol).max(0).min(8)

    gameState = gameState.selectCell(nextRow, nextCol)
    gameStateProperty.value = gameState
  }

  def updateTime(): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    gameState = gameState.incrementTime()
    gameStateProperty.value = gameState
  }

  def toggleNotesMode(): Unit = {
    if (gameState == null || gameState.isGameOver || gameState.isPaused) return

    gameState = gameState.copy(isNotesMode = !gameState.isNotesMode)
    gameStateProperty.value = gameState
  }

  def togglePause(): Unit = {
    if (gameState == null || gameState.isGameOver) return

    gameState = gameState.copy(isPaused = !gameState.isPaused)
    gameStateProperty.value = gameState
  }
}
