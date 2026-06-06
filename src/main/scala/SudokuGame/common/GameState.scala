package SudokuGame.common

enum GameStatus:
  case InProgress, Finished

case class GameState(
  createdAt: String,
  difficulty: String,
  elapsedSeconds: Long,
  board: Seq[Seq[Int]],
  status: GameStatus,
  gameId: String = "",
  initialBoard: Seq[Seq[Int]] = Seq.empty,
  notes: Seq[Seq[Seq[Int]]] = Seq.empty,
  errorCount: Int = 0,
  hintsRemaining: Int = 3
) {
  def resumeInitialBoard: Seq[Seq[Int]] =
    if initialBoard.nonEmpty then initialBoard else board

  def normalizedNotes: Seq[Seq[Seq[Int]]] =
    if notes.nonEmpty then notes
    else Seq.fill(9, 9)(Seq.empty[Int])
}
