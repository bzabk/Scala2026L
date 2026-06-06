package SudokuGame.ui.views

import SudokuGame.model.SudokuSolver

class SudokuPuzzlesTest extends munit.FunSuite {
  private def toSeq(board: Array[Array[Int]]): Seq[Seq[Int]] =
    board.map(_.toSeq).toSeq

  test("provides a puzzle for every difficulty") {
    assertEquals(SudokuPuzzles.all.keySet, Difficulty.values.toSet)
  }

  test("puzzles have valid sudoku dimensions and can be solved") {
    SudokuPuzzles.all.foreach { case (difficulty, board) =>
      assertEquals(board.length, 9, s"$difficulty should have 9 rows")
      assert(
        board.forall(_.length == 9),
        s"$difficulty should have 9 columns in each row"
      )
      assert(
        SudokuSolver.solve(toSeq(board)).isDefined,
        s"$difficulty should be solvable"
      )
    }
  }

  test("difficulty boards are distinct and returned as defensive copies") {
    val encodedBoards = SudokuPuzzles.all.values.map(toSeq).toSet
    assertEquals(encodedBoards.size, Difficulty.values.size)

    val firstCopy = SudokuPuzzles.boardFor(Difficulty.Easy)
    firstCopy(0)(0) = 0

    val secondCopy = SudokuPuzzles.boardFor(Difficulty.Easy)
    assertEquals(secondCopy(0)(0), 5)
  }
}
