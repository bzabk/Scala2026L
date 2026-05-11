package SudokuGame.model

case class SudokuCell(
    value: Int = 0, // 0 means empty
    isGiven: Boolean = false // true if this was given in the puzzle
)

class SudokuBoard(initialBoard: Array[Array[Int]] = Array.ofDim[Int](9, 9)) {
  private val _board: Array[Array[SudokuCell]] = Array.ofDim[SudokuCell](9, 9)
  var conflicts: Array[Array[Boolean]] = Array.ofDim[Boolean](9, 9)

  for {
    row <- 0 until 9
    col <- 0 until 9
  } {
    val value = initialBoard(row)(col)
    _board(row)(col) = SudokuCell(value = value, isGiven = value != 0)
    conflicts(row)(col) = false
  }

  def getCell(row: Int, col: Int): SudokuCell = _board(row)(col)

  def isCellGiven(row: Int, col: Int): Boolean = _board(row)(col).isGiven

  def getCellValue(row: Int, col: Int): Int = _board(row)(col).value

  def getAllCells(): Array[Array[SudokuCell]] = _board

  def move(row: Int, col: Int, value: Int): Unit = {
    val previousValue = _board(row)(col).value

    _board(row)(col) = _board(row)(col).copy(value = value)

    _updateConflictsForValue(row, col, previousValue)
    _updateConflictsForValue(row, col, value)
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

  private def _hasConflictAt(row: Int, col: Int): Boolean = {
    val value = _board(row)(col).value
    if (value == 0) false
    else {
      val rowMatches = (0 until 9).count(c => _board(row)(c).value == value)
      val colMatches = (0 until 9).count(r => _board(r)(col).value == value)
      val boxMatches =
        (for {
          r <- (row / 3) * 3 until (row / 3) * 3 + 3
          c <- (col / 3) * 3 until (col / 3) * 3 + 3
        } yield _board(r)(c).value).count(_ == value)

      rowMatches > 1 || colMatches > 1 || boxMatches > 1
    }
  }

  private def _updateConflictsForValue(row: Int, col: Int, value: Int): Unit = {
    val cols = (0 until 9)
      .filter(c => _board(row)(c).value == value)
      .map((c) => (row, c))

    val rows = (0 until 9)
      .filter(r => _board(r)(col).value == value)
      .map((r) => (r, col))

    val boxRowCols = for {
      r <- (row / 3) * 3 until (row / 3) * 3 + 3
      c <- (col / 3) * 3 until (col / 3) * 3 + 3
      if _board(r)(c).value == value
    } yield (r, c)

    val distinctRowCols = (cols ++ rows ++ boxRowCols).distinct

    distinctRowCols.foreach((r, c) => conflicts(r)(c) = _hasConflictAt(r, c))
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
