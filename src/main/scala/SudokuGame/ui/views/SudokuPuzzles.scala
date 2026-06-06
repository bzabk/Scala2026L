package SudokuGame.ui.views

object SudokuPuzzles {
  private val boardsByDifficulty: Map[Difficulty, Array[Array[Int]]] = Map(
    Difficulty.Easy -> Array(
      Array(5, 3, 0, 0, 7, 0, 0, 0, 0),
      Array(6, 0, 0, 1, 9, 5, 0, 0, 0),
      Array(0, 9, 8, 0, 0, 0, 0, 6, 0),
      Array(8, 0, 0, 0, 6, 0, 0, 0, 3),
      Array(4, 0, 0, 8, 0, 3, 0, 0, 1),
      Array(7, 0, 0, 0, 2, 0, 0, 0, 6),
      Array(0, 6, 0, 0, 0, 0, 2, 8, 0),
      Array(0, 0, 0, 4, 1, 9, 0, 0, 5),
      Array(0, 0, 0, 0, 8, 0, 0, 7, 9)
    ),
    Difficulty.Medium -> Array(
      Array(0, 2, 0, 6, 0, 8, 0, 0, 0),
      Array(5, 8, 0, 0, 0, 9, 7, 0, 0),
      Array(0, 0, 0, 0, 4, 0, 0, 0, 0),
      Array(3, 7, 0, 0, 0, 0, 5, 0, 0),
      Array(6, 0, 0, 0, 0, 0, 0, 0, 4),
      Array(0, 0, 8, 0, 0, 0, 0, 1, 3),
      Array(0, 0, 0, 0, 2, 0, 0, 0, 0),
      Array(0, 0, 9, 8, 0, 0, 0, 3, 6),
      Array(0, 0, 0, 3, 0, 6, 0, 9, 0)
    ),
    Difficulty.Hard -> Array(
      Array(0, 0, 0, 0, 0, 0, 2, 0, 0),
      Array(0, 8, 0, 0, 0, 7, 0, 9, 0),
      Array(6, 0, 2, 0, 0, 0, 5, 0, 0),
      Array(0, 7, 0, 0, 6, 0, 0, 0, 0),
      Array(0, 0, 0, 9, 0, 1, 0, 0, 0),
      Array(0, 0, 0, 0, 2, 0, 0, 4, 0),
      Array(0, 0, 5, 0, 0, 0, 6, 0, 3),
      Array(0, 9, 0, 4, 0, 0, 0, 7, 0),
      Array(0, 0, 6, 0, 0, 0, 0, 0, 0)
    ),
    Difficulty.Expert -> Array(
      Array(8, 0, 0, 0, 0, 0, 0, 0, 0),
      Array(0, 0, 3, 6, 0, 0, 0, 0, 0),
      Array(0, 7, 0, 0, 9, 0, 2, 0, 0),
      Array(0, 5, 0, 0, 0, 7, 0, 0, 0),
      Array(0, 0, 0, 0, 4, 5, 7, 0, 0),
      Array(0, 0, 0, 1, 0, 0, 0, 3, 0),
      Array(0, 0, 1, 0, 0, 0, 0, 6, 8),
      Array(0, 0, 8, 5, 0, 0, 0, 1, 0),
      Array(0, 9, 0, 0, 0, 0, 4, 0, 0)
    )
  )

  def boardFor(difficulty: Difficulty): Array[Array[Int]] =
    boardsByDifficulty.getOrElse(difficulty, boardsByDifficulty(Difficulty.Easy))
      .map(_.clone())

  def all: Map[Difficulty, Array[Array[Int]]] =
    boardsByDifficulty.map { case (difficulty, board) =>
      difficulty -> board.map(_.clone())
    }
}
