package SudokuGame.game.service

import SudokuGame.common.GameState
import SudokuGame.game.repository.GameRepository

import scala.concurrent.{ExecutionContext, Future}

class GameService(gameRepository: GameRepository)(implicit ec: ExecutionContext) {

  def loadRecentGames(username: String): Future[List[GameState]] =
    gameRepository.fetchRecentGames(username)
}
