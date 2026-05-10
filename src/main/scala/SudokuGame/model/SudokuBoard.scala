package SudokuGame.model

case class SudokuCell(
    value: Int = 0, // 0 means empty
    isGiven: Boolean = false // true if this was given in the puzzle
)

class SudokuBoard(initialBoard: Array[Array[Int]] = Array.ofDim[Int](9, 9)) {
  private val board: Array[Array[SudokuCell]] = Array.ofDim[SudokuCell](9, 9)
  var conflicts: Array[Array[Boolean]] = Array.ofDim[Boolean](9, 9)

  for {
    row <- 0 until 9
    col <- 0 until 9
  } {
    val value = initialBoard(row)(col)
    board(row)(col) = SudokuCell(value = value, isGiven = value != 0)
    conflicts(row)(col) = false
  }

  def getCell(row: Int, col: Int): SudokuCell = board(row)(col)

  def isCellGiven(row: Int, col: Int): Boolean = board(row)(col).isGiven

  def getCellValue(row: Int, col: Int): Int = board(row)(col).value

  def getAllCells: Array[Array[SudokuCell]] = board

  private def _hasDuplicates(values: Seq[Int]): Boolean = {
    val nonZero = values.filter(_ != 0)
    nonZero.size != nonZero.toSet.size
  }

  def _getRow(row: Int): IndexedSeq[Int] =
    (0 until 9).map(col => board(row)(col).value)

  def _getColumn(col: Int): IndexedSeq[Int] =
    (0 until 9).map(row => board(row)(col).value)

  def _getBox(boxRow: Int, boxCol: Int): IndexedSeq[Int] =
    for {
      r <- boxRow * 3 until boxRow * 3 + 3
      c <- boxCol * 3 until boxCol * 3 + 3
    } yield board(r)(c).value

  def _isRowValid(row: Int): Boolean = {
    !_hasDuplicates(_getRow(row))
  }

  def _isColumnValid(col: Int): Boolean = {
    !_hasDuplicates(_getColumn(col))
  }

  def _isBoxValid(boxRow: Int, boxCol: Int): Boolean = {
    !_hasDuplicates(_getBox(boxRow, boxCol))
  }

  def isValid: Boolean = {
    (0 until 9).forall(_isRowValid) &&
    (0 until 9).forall(_isColumnValid) &&
    (0 until 3).forall(boxRow =>
      (0 until 3).forall(boxCol => _isBoxValid(boxRow, boxCol))
    )
  }

  def isSolved: Boolean = {
    board.flatten.forall(_.value != 0) && isValid
  }

  // This validation assumes value is in range [1, 9]
  def _isMoveValid(row: Int, col: Int, value: Int): Boolean = {
    !_getRow(row).contains(value) && !_getColumn(col).contains(
      value
    ) && !_getBox(
      row / 3,
      col / 3
    ).contains(value)
  }

  def move(row: Int, col: Int, value: Int): Unit = {
    val hadConflicts = conflicts(row)(col)
    val previousValue = board(row)(col).value

    board(row)(col) = board(row)(col).copy(value = value)

    if (hadConflicts) _updateConflictsForValue(row, col, previousValue)

    _updateConflictsForValue(row, col, value)
  }

  def _updateConflictsForValue(row: Int, col: Int, value: Int): Unit = {
    val cols = (0 until 9).filter(c => board(row)(c).value == value)

    cols match {
      case Seq()  => ()
      case Seq(c) =>
        conflicts(row)(c) = false
      case many =>
        many.foreach(c => conflicts(row)(c) = true)
    }

    val rows = (0 until 9).filter(r => board(r)(col).value == value)

    rows match {
      case Seq()  => ()
      case Seq(r) =>
        conflicts(r)(col) = false
      case many =>
        many.foreach(r => conflicts(r)(col) = true)
    }

    val boxRowCols = for {
      r <- (row / 3) * 3 until (row / 3) * 3 + 3
      c <- (col / 3) * 3 until (col / 3) * 3 + 3
      if board(r)(c).value == value
    } yield (r, c)

    boxRowCols match {
      case Seq()       => ()
      case Seq((r, c)) =>
        conflicts(r)(c) = false
      case many =>
        many.foreach((r, c) => conflicts(r)(c) = true)
    }
  }
}
