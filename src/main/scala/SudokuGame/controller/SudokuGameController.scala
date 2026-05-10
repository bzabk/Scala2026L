package SudokuGame.controller

import SudokuGame.model.{GameState, SudokuBoard}
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
    if (
      gameState == null || gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty
    ) return

    if (value < 1 || value > 9) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (value == gameState.board.getCellValue(row, col))
      return

    if (!gameState.board.isCellGiven(row, col)) {
      gameState.board.move(row, col, value)

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
  }

  def clearCell(): Unit = {
    if (
      gameState == null || gameState.selectedRow.isEmpty || gameState.selectedCol.isEmpty
    ) return

    val row = gameState.selectedRow.get
    val col = gameState.selectedCol.get

    if (!gameState.board.isCellGiven(row, col)) {
      gameState.board.move(row, col, 0)
      gameStateProperty.value = gameState
    }
  }

  def selectCell(row: Int, col: Int): Unit = {
    if (gameState != null) {
      gameState = gameState.selectCell(row, col)
      gameStateProperty.value = gameState
    }
  }

  def updateTime(): Unit = {
    if (gameState != null && !gameState.isGameOver) {
      gameState = gameState.incrementTime
      gameStateProperty.value = gameState
    }
  }
}
