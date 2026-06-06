package SudokuGame.model

object SudokuSolver {
  private val BoardSize = 9
  private val BoxSize = 3

  def solve(board: Seq[Seq[Int]]): Option[Seq[Seq[Int]]] = {
    val grid = Array.tabulate(BoardSize, BoardSize) { (row, col) =>
      board.lift(row).flatMap(_.lift(col)).getOrElse(0)
    }

    if !isConsistent(grid) then None
    else if solveInPlace(grid) then Some(grid.map(_.toSeq).toSeq)
    else None
  }

  private def solveInPlace(grid: Array[Array[Int]]): Boolean = {
    nextEmptyCell(grid) match {
      case None => true
      case Some((row, col, candidates)) =>
        candidates.exists { value =>
          grid(row)(col) = value
          val solved = solveInPlace(grid)
          if !solved then grid(row)(col) = 0
          solved
        }
    }
  }

  private def nextEmptyCell(
      grid: Array[Array[Int]]
  ): Option[(Int, Int, Seq[Int])] = {
    val emptyCells = for {
      row <- 0 until BoardSize
      col <- 0 until BoardSize
      if grid(row)(col) == 0
    } yield (row, col, candidatesFor(grid, row, col))

    emptyCells.sortBy(_._3.size).headOption
  }

  private def candidatesFor(
      grid: Array[Array[Int]],
      row: Int,
      col: Int
  ): Seq[Int] =
    (1 to 9).filter(value => canPlace(grid, row, col, value))

  private def isConsistent(grid: Array[Array[Int]]): Boolean =
    (0 until BoardSize).forall(row =>
      (0 until BoardSize).forall(col =>
        grid(row)(col) == 0 || canPlace(grid, row, col, grid(row)(col))
      )
    )

  private def canPlace(
      grid: Array[Array[Int]],
      row: Int,
      col: Int,
      value: Int
  ): Boolean = {
    if value < 1 || value > 9 then false
    else {
      val rowValid =
        (0 until BoardSize).forall(c => c == col || grid(row)(c) != value)
      val colValid =
        (0 until BoardSize).forall(r => r == row || grid(r)(col) != value)
      val boxRow = (row / BoxSize) * BoxSize
      val boxCol = (col / BoxSize) * BoxSize
      val boxValid = (for {
        r <- boxRow until boxRow + BoxSize
        c <- boxCol until boxCol + BoxSize
      } yield (r, c)).forall { case (r, c) =>
        (r == row && c == col) || grid(r)(c) != value
      }

      rowValid && colValid && boxValid
    }
  }
}
