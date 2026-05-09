package SudokuGame

import SudokuGame.auth.repository.HttpUserRepository
import SudokuGame.auth.service.AuthService
import SudokuGame.common.AppState
import SudokuGame.controller.{AuthController, GameController}
import SudokuGame.game.repository.GameRepository
import SudokuGame.game.service.GameService
import SudokuGame.ui.layout.{MainApplicationLayout, SidebarView}
import SudokuGame.ui.views.DashboardView
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Screen

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends JFXApp3 {
  override def start(): Unit = {
    val appState       = new AppState()
    val appLayout      = new MainApplicationLayout()
    val authService    = new AuthService(new HttpUserRepository())
    val gameService    = new GameService(new GameRepository())
    val gameController = new GameController(appLayout, gameService, appState)
    val authController = new AuthController(appLayout, authService, appState,
      onLoginSuccess = gameController.loadRecentGames
    )
    val sidebar = new SidebarView(authController,appState,appLayout)
    val dashboard = new DashboardView(authController,appState)
    appLayout.setSidebar(sidebar.view)
    appLayout.setMainContent(dashboard.view)

    val bounds = Screen.primary.visualBounds

    stage = new JFXApp3.PrimaryStage {
      title = "ScalaFX Sudoku"
      resizable = true
      minWidth = 900
      minHeight = 600

      x = bounds.minX
      y = bounds.minY
      width = bounds.width
      height = bounds.height

      scene = new Scene {
        root = appLayout.view
      }
    }
  }
}
