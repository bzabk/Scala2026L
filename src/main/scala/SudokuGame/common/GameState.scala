package SudokuGame.common

enum GameStatus:
  case InProgress, Finished

case class GameState(
  createdAt: String,
  difficulty: String,
  elapsedSeconds: Long,
  board: Seq[Seq[Int]],
  status: GameStatus
)