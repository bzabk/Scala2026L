package SudokuGame.model
import scala.collection.immutable as immutable
import scala.collection.mutable as mutable

case class BoardMove(
    row: Int,
    col: Int,
    previousValue: Int,
    previousNotes: immutable.SortedSet[Int],
    newValue: Int,
    newNotes: immutable.SortedSet[Int]
)

case class GameState(
    board: SudokuBoard,
    elapsedSeconds: Int = 0,
    errorCount: Int = 0,
    maxErrors: Int = 3,
    selectedRow: Option[Int] = None,
    selectedCol: Option[Int] = None,
    moveHistory: List[BoardMove] = Nil,
    redoHistory: List[BoardMove] = Nil,
    isNotesMode: Boolean = false,
    isPaused: Boolean = false,
    isGameOver: Boolean = false
) {
  def selectCell(row: Int, col: Int): GameState = {
    this.copy(selectedRow = Some(row), selectedCol = Some(col))
  }

  def clearSelection(): GameState = {
    this.copy(selectedRow = None, selectedCol = None)
  }

  def incrementTime(): GameState = {
    this.copy(elapsedSeconds = elapsedSeconds + 1)
  }

  def recordMove(move: BoardMove): GameState = {
    this.copy(moveHistory = move :: moveHistory, redoHistory = Nil)
  }

  def popUndoMove(): (GameState, Option[BoardMove]) = {
    moveHistory match {
      case move :: remaining =>
        (
          this.copy(
            moveHistory = remaining,
            redoHistory = move :: redoHistory
          ),
          Some(move)
        )
      case _ => (this, None)
    }
  }

  def popRedoMove(): (GameState, Option[BoardMove]) = {
    redoHistory match {
      case move :: remaining =>
        (
          this.copy(
            moveHistory = move :: moveHistory,
            redoHistory = remaining
          ),
          Some(move)
        )
      case Nil => (this, None)
    }
  }

  def formatTime(): String = {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    f"$minutes%02d:$seconds%02d"
  }

  def canUndo: Boolean = moveHistory.nonEmpty

  def canRedo: Boolean = redoHistory.nonEmpty

  def conflicts: Array[Array[mutable.Set[Int]]] =
    board.conflicts

  def isSolved: Boolean = board.isSolved

  def isLost: Boolean = errorCount >= maxErrors
}
