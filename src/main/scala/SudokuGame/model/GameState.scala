package SudokuGame.model

case class GameState(
    board: SudokuBoard,
    elapsedSeconds: Int = 0,
    errorCount: Int = 0,
    maxErrors: Int = 3,
    selectedRow: Option[Int] = None,
    selectedCol: Option[Int] = None,
    isPaused: Boolean = false,
    isGameOver: Boolean = false
) {
  def selectCell(row: Int, col: Int): GameState = {
    this.copy(selectedRow = Some(row), selectedCol = Some(col))
  }

  def clearSelection: GameState = {
    this.copy(selectedRow = None, selectedCol = None)
  }

  def incrementTime: GameState = {
    this.copy(elapsedSeconds = elapsedSeconds + 1)
  }

  def formatTime: String = {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    f"$minutes%02d:$seconds%02d"
  }

  def conflicts: Array[Array[Boolean]] = board.conflicts

  def isSolved: Boolean = board.isSolved

  def isLost: Boolean = errorCount >= maxErrors
}
