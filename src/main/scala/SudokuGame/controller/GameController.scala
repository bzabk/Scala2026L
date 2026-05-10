package SudokuGame.controller

import SudokuGame.common.AppState
import SudokuGame.game.service.GameService
import SudokuGame.ui.layout.MainApplicationLayout
import scalafx.application.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class GameController(
  mainLayout:  MainApplicationLayout,
  gameService: GameService,
  appState:    AppState
) {

  appState.currentUser.onChange { (_, _, userOpt) =>
    if userOpt.isEmpty then appState.recentGames.set(List.empty)
  }

  def loadRecentGames(username: String): Unit = {
    gameService.loadRecentGames(username).onComplete {
      case Success(games) =>
        Platform.runLater {
          appState.recentGames.set(games)
        }
      case Failure(_) =>
        Platform.runLater {
          appState.recentGames.set(List.empty)
        }
    }
  }
  
}