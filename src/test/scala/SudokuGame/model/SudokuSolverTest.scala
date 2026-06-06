package SudokuGame.model

class SudokuSolverTest extends munit.FunSuite {
  private val puzzle = Seq(
    Seq(5, 3, 0, 0, 7, 0, 0, 0, 0),
    Seq(6, 0, 0, 1, 9, 5, 0, 0, 0),
    Seq(0, 9, 8, 0, 0, 0, 0, 6, 0),
    Seq(8, 0, 0, 0, 6, 0, 0, 0, 3),
    Seq(4, 0, 0, 8, 0, 3, 0, 0, 1),
    Seq(7, 0, 0, 0, 2, 0, 0, 0, 6),
    Seq(0, 6, 0, 0, 0, 0, 2, 8, 0),
    Seq(0, 0, 0, 4, 1, 9, 0, 0, 5),
    Seq(0, 0, 0, 0, 8, 0, 0, 7, 9)
  )

  test("solves a valid sudoku puzzle") {
    val solution = SudokuSolver.solve(puzzle)

    assert(solution.isDefined)
    assertEquals(solution.get.head, Seq(5, 3, 4, 6, 7, 8, 9, 1, 2))
  }

  test("rejects an inconsistent puzzle") {
    val inconsistent = puzzle.updated(0, Seq(5, 5, 0, 0, 7, 0, 0, 0, 0))

    assertEquals(SudokuSolver.solve(inconsistent), None)
  }

  test("returns an already solved valid board unchanged") {
    val solved = Seq(
      Seq(5, 3, 4, 6, 7, 8, 9, 1, 2),
      Seq(6, 7, 2, 1, 9, 5, 3, 4, 8),
      Seq(1, 9, 8, 3, 4, 2, 5, 6, 7),
      Seq(8, 5, 9, 7, 6, 1, 4, 2, 3),
      Seq(4, 2, 6, 8, 5, 3, 7, 9, 1),
      Seq(7, 1, 3, 9, 2, 4, 8, 5, 6),
      Seq(9, 6, 1, 5, 3, 7, 2, 8, 4),
      Seq(2, 8, 7, 4, 1, 9, 6, 3, 5),
      Seq(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )

    assertEquals(SudokuSolver.solve(solved), Some(solved))
  }

  test("rejects values outside the sudoku range") {
    val invalid = puzzle.updated(0, Seq(10, 3, 0, 0, 7, 0, 0, 0, 0))

    assertEquals(SudokuSolver.solve(invalid), None)
  }
}
