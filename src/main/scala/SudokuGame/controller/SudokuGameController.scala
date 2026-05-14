package SudokuGame.controller

import SudokuGame.model.{BoardMove, GameState, SudokuBoard}
import scalafx.beans.property.ObjectProperty

class SudokuGameController {
  private val _gameStateProperty =
    new ObjectProperty[GameState](this, "gameState", null)

  def gameState: GameState = _gameStateProperty.value
  def gameState_=(state: GameState): Unit = _gameStateProperty.value = state

  def gameStateProperty: ObjectProperty[GameState] = _gameStateProperty

  def startNewGame(initialBoard: Array[Array[Int]]): Unit = {
    val board = new SudokuBoard(initialBoard)
    gameState = GameState(board = board)
  }

  def placeNumber(value: Int): Unit = {
    if (gameState == null || gameState.isGameOver) return

    if (gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty) return

    if (value < 1 || value > 9) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (gameState.board.isCellGiven(row, col)) return

    val previousValue = gameState.board.getCellValue(row, col)
    if (value == previousValue)
      return

    gameState.board.move(row, col, value)
    gameState = gameState.recordMove(BoardMove(row, col, previousValue, value))

    if (gameState.conflicts(row)(col)) {
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
    if (gameState == null || gameState.isGameOver) return

    if (gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (gameState.board.isCellGiven(row, col)) return

    val previousValue = gameState.board.getCellValue(row, col)
    if (previousValue == 0) return

    gameState.board.move(row, col, 0)
    gameState = gameState.recordMove(BoardMove(row, col, previousValue, 0))

    gameStateProperty.value = gameState
  }

  def undo(): Unit = {
    if (gameState == null || gameState.isGameOver) return

    val (updatedState, moveOpt) = gameState.popUndoMove()
    moveOpt match {
      case Some(move) =>
        gameState.board.move(move.row, move.col, move.previousValue)
      case None => ()
    }

    gameState = updatedState
    gameStateProperty.value = gameState
  }

  def redo(): Unit = {
    if (gameState == null || gameState.isGameOver) return

    val (updatedState, moveOpt) = gameState.popRedoMove()
    moveOpt match {
      case Some(move) =>
        gameState.board.move(move.row, move.col, move.newValue)
      case None => ()
    }

    gameState = updatedState
    gameStateProperty.value = gameState
  }

  def selectCell(row: Int, col: Int): Unit = {
    if (gameState == null || gameState.isGameOver) return

    gameState = gameState.selectCell(row, col)
    gameStateProperty.value = gameState
  }

  def updateTime(): Unit = {
    if (gameState == null || gameState.isGameOver) return

    gameState = gameState.incrementTime()
    gameStateProperty.value = gameState
  }
}
