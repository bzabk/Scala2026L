package SudokuGame.model

import scala.collection.immutable as immutable
import scala.collection.mutable as mutable

case class SudokuCell(
    value: Int = 0, // 0 means empty
    notes: immutable.SortedSet[Int] = immutable.SortedSet.empty,
    isGiven: Boolean = false // true if this was given in the puzzle
)

class SudokuBoard(initialBoard: Array[Array[Int]] = Array.ofDim[Int](9, 9)) {
  private val _board: Array[Array[SudokuCell]] = Array.ofDim[SudokuCell](9, 9)
  var conflicts: Array[Array[mutable.Set[Int]]] =
    Array.ofDim[mutable.Set[Int]](9, 9)

  for {
    row <- 0 until 9
    col <- 0 until 9
  } {
    val value = initialBoard(row)(col)
    _board(row)(col) = SudokuCell(value = value, isGiven = value != 0)
    conflicts(row)(col) = mutable.Set.empty
  }

  def getCell(row: Int, col: Int): SudokuCell = _board(row)(col)

  def isCellGiven(row: Int, col: Int): Boolean = _board(row)(col).isGiven

  def getCellValue(row: Int, col: Int): Int = _board(row)(col).value

  def getCellNotes(row: Int, col: Int): immutable.SortedSet[Int] =
    _board(row)(col).notes

  def getAllCells(): Array[Array[SudokuCell]] = _board

  def updateCell(row: Int, col: Int, value: Int, isNotesMode: Boolean): Unit = {
    val previousValue = _board(row)(col).value

    if (isNotesMode) {
      var updatedNotes = _board(row)(col).notes

      if (updatedNotes.contains(value)) {
        updatedNotes = updatedNotes - value
      } else {
        if (previousValue != 0) {
          updatedNotes = updatedNotes + previousValue
        }
        updatedNotes = updatedNotes + value
      }
      _board(row)(col) = _board(row)(col).copy(value = 0, notes = updatedNotes)
    } else {
      _board(row)(col) = _board(row)(col)
        .copy(value = value, notes = immutable.SortedSet.empty)
    }

    _updateConflictsForCell(row, col)
  }

  def updateCell(
      row: Int,
      col: Int,
      value: Int,
      notes: immutable.SortedSet[Int]
  ): Unit = {
    val previousValue = _board(row)(col).value

    _board(row)(col) = _board(row)(col).copy(value = value, notes = notes)

    _updateConflictsForCell(row, col)
  }

  private def _hasDuplicates(values: Seq[Int]): Boolean = {
    val nonZero = values.filter(_ != 0)
    nonZero.size != nonZero.toSet.size
  }

  private def _getRow(row: Int): IndexedSeq[Int] =
    (0 until 9).map(col => _board(row)(col).value)

  private def _getColumn(col: Int): IndexedSeq[Int] =
    (0 until 9).map(row => _board(row)(col).value)

  private def _getBox(boxRow: Int, boxCol: Int): IndexedSeq[Int] =
    for {
      r <- boxRow * 3 until boxRow * 3 + 3
      c <- boxCol * 3 until boxCol * 3 + 3
    } yield _board(r)(c).value

  private def _isRowValid(row: Int): Boolean = {
    !_hasDuplicates(_getRow(row))
  }

  private def _isColumnValid(col: Int): Boolean = {
    !_hasDuplicates(_getColumn(col))
  }

  private def _isBoxValid(boxRow: Int, boxCol: Int): Boolean = {
    !_hasDuplicates(_getBox(boxRow, boxCol))
  }

  // This validation assumes value is in range [1, 9]
  private def _isMoveValid(row: Int, col: Int, value: Int): Boolean = {
    !_getRow(row).contains(value) && !_getColumn(col).contains(
      value
    ) && !_getBox(
      row / 3,
      col / 3
    ).contains(value)
  }

  private def _hasConflictAt(
      row: Int,
      col: Int,
      value: Int,
      threshold: Int
  ): Boolean = {
    if (value == 0) false
    else {
      val rowMatches = (0 until 9).count(c => _board(row)(c).value == value)
      val colMatches = (0 until 9).count(r => _board(r)(col).value == value)
      val boxMatches =
        (for {
          r <- (row / 3) * 3 until (row / 3) * 3 + 3
          c <- (col / 3) * 3 until (col / 3) * 3 + 3
        } yield _board(r)(c).value).count(_ == value)

      rowMatches > threshold || colMatches > threshold || boxMatches > threshold
    }
  }

  private def _cellsToRecalculate(row: Int, col: Int): Seq[(Int, Int)] = {
    val rowCells = (0 until 9).map(c => (row, c))
    val colCells = (0 until 9).map(r => (r, col))
    val boxCells = for {
      r <- (row / 3) * 3 until (row / 3) * 3 + 3
      c <- (col / 3) * 3 until (col / 3) * 3 + 3
    } yield (r, c)

    (rowCells ++ colCells ++ boxCells).distinct
  }

  private def _valuesToCheckForCell(
      row: Int,
      col: Int,
      selectedCell: Boolean
  ): Seq[Int] = {
    val cell = _board(row)(col)

    if (selectedCell && cell.notes.nonEmpty) cell.notes.toSeq
    else if (cell.value != 0) Seq(cell.value)
    else Seq.empty
  }

  private def _updateConflictsForCell(row: Int, col: Int): Unit = {
    _cellsToRecalculate(row, col).foreach { case (currentRow, currentCol) =>
      val cell = _board(currentRow)(currentCol)
      val selectedCell = currentRow == row && currentCol == col
      val threshold = if (selectedCell && cell.notes.nonEmpty) 0 else 1

      conflicts(currentRow)(currentCol).clear()

      _valuesToCheckForCell(currentRow, currentCol, selectedCell).foreach {
        value =>
          if (_hasConflictAt(currentRow, currentCol, value, threshold)) {
            conflicts(currentRow)(currentCol).add(value)
          }
      }
    }
  }

  def isValid: Boolean = {
    (0 until 9).forall(_isRowValid) &&
    (0 until 9).forall(_isColumnValid) &&
    (0 until 3).forall(boxRow =>
      (0 until 3).forall(boxCol => _isBoxValid(boxRow, boxCol))
    )
  }

  def isSolved: Boolean = {
    _board.flatten.forall(_.value != 0) && isValid
  }
}
