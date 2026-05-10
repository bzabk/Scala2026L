package SudokuGame.ui.layout
import SudokuGame.common.{AppState, GameState, GameStatus}
import javafx.event.ActionEvent
import SudokuGame.controller.AuthController
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class SidebarView(authController: AuthController,appState: AppState,mainApplicationLayout: MainApplicationLayout) {

  private val sidebarBgColor = "#1d2a3d"
  private val borderColor = "#324461"
  private val borderLineThickness = "3"
  private val loginButtonStyle =
    """
      -fx-background-color: #2563EB;
      -fx-text-fill: white;
      -fx-font-size: 18px;
      -fx-font-weight: bold;
      -fx-background-radius: 12;
      -fx-cursor: hand;
    """

  private val logoHBox = new HBox {
    spacing = 10
    padding = Insets(20, 20, 20, 20)
    alignment = Pos.Center
    children = Seq(
      new Label("ScalaFX Sudoku") {
        style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;"
      }
    )
    style = s"-fx-border-color: $borderColor; -fx-border-width: 0 0 $borderLineThickness 0;"
  }

  private val spacer = new Region {
    vgrow = Priority.Always
  }

  private def recentGameRow(difficulty: String, color: String, date: String, time: String, status: String) = {
    val row = new HBox {
      spacing = 10
      padding = Insets(9, 8, 9, 8)
      alignment = Pos.CenterLeft
      style = "-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;"
      children = Seq(
        new Label("●") {
          style = s"-fx-text-fill: $color; -fx-font-size: 9px;"
          padding = Insets(2, 0, 0, 0)
        },
        new VBox {
          spacing = 2
          HBox.setHgrow(this, Priority.Always)
          children = Seq(
            new Label(difficulty) {
              style = s"-fx-text-fill: $color; -fx-font-size: 12px; -fx-font-weight: bold;"
            },
            new Label(status) {
              style = "-fx-text-fill: #6b7280; -fx-font-size: 10px;"
            }
          )
        },
        new VBox {
          alignment = Pos.CenterRight
          spacing = 2
          children = Seq(
            new Label(date) { style = "-fx-text-fill: #9ca3af; -fx-font-size: 10px;" },
            new Label(time) { style = "-fx-text-fill: #6b7280; -fx-font-size: 10px;" }
          )
        }
      )
    }
    row.delegate.setOnMouseEntered(_ => row.style = "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-cursor: hand;")
    row.delegate.setOnMouseExited(_  => row.style = "-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;")
    row
  }

  val recentGamesList = new VBox {
    spacing = 4
    alignment = Pos.Center
    VBox.setVgrow(this, Priority.Always)
    style = "-fx-background-color: #111827; -fx-background-radius: 8; -fx-border-color: #324461; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 8;"
  }

  private val recentGamesSection = new VBox {
    padding = Insets(16, 15, 12, 15)
    visible = false
    managed = false
    VBox.setVgrow(this, Priority.Always)
    children = Seq(
      new Label("Recent Games:") {
        style = "-fx-text-fill: #4ade80; -fx-font-size: 23px; -fx-font-weight: bold;"
        padding = Insets(0, 0, 6, 4)
        maxWidth = Double.MaxValue
        alignment = Pos.Center
      },
      recentGamesList
    )
  }

  private def difficultyColor(difficulty: String): String = difficulty match {
    case "Easy"   => "#4ade80"
    case "Medium" => "#fbbf24"
    case "Hard"   => "#f87171"
    case "Expert" => "#c084fc"
    case _        => "#9ca3af"
  }

  private def gameStateToRow(game: GameState) = {
    val date   = game.createdAt.take(10)
    val time   = game.createdAt.drop(11).take(5)
    val status = if game.status == GameStatus.Finished then "Completed" else "In progress"
    recentGameRow(game.difficulty, difficultyColor(game.difficulty), date, time, status)
  }

  private def updateRecentGamesDisplay(games: List[GameState]): Unit =
    recentGamesList.children = if games.nonEmpty then games.map(gameStateToRow) else Seq(new Label("No recent games found") {
      style = "-fx-text-fill: #9ca3af; -fx-font-size: 12px;"
      maxWidth = Double.MaxValue
      alignment = Pos.Center
    })

  appState.currentUser.onChange { (_, _, userOpt) =>
    val loggedIn = userOpt.isDefined
    recentGamesSection.visible = loggedIn
    recentGamesSection.managed = loggedIn
    spacer.visible = !loggedIn
    spacer.managed = !loggedIn
    if loggedIn then updateRecentGamesDisplay(List.empty)
  }

  appState.recentGames.onChange { (_, _, games) =>
    updateRecentGamesDisplay(games)
  }

  private def signInButton = new Button("Sign In") {
    maxWidth = Double.MaxValue
    alignment = Pos.Center
    padding = Insets(10, 15, 10, 15)
    style = loginButtonStyle
    onAction = (_: ActionEvent) => authController.showLoginView()
  }

  private def signOutButton = new Button("Sign Out") {
    maxWidth = Double.MaxValue
    alignment = Pos.Center
    padding = Insets(10, 15, 10, 15)
    style = loginButtonStyle
    onAction = (_: ActionEvent) => authController.signOut()
  }

  private def delay(time: Long) = {
    new Thread(() => {
      Thread.sleep(time)
    }).start()
  }

  private val loginVBoX = new VBox {
    spacing = 10
    padding = Insets(20, 15, 20, 15)
    style = s"-fx-border-color: $borderColor; -fx-border-width:  $borderLineThickness 0 0 0;"
    children = Seq(signInButton)
  }

  appState.currentUser.onChange { (_, _, userOpt) =>
    loginVBoX.children = userOpt match {
      case Some(_) => Seq(signOutButton)
      case None    => Seq(signInButton)
    }
  }

  val view: VBox = new VBox {
    style = s"-fx-background-color: $sidebarBgColor; -fx-border-color: $borderColor; -fx-border-width: 0 $borderLineThickness 0 0;"
    prefWidth = 260
    children = Seq(logoHBox, recentGamesSection, spacer, loginVBoX)
  }
}
