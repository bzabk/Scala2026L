package SudokuGame.ui.views

import SudokuGame.common.AppState
import SudokuGame.controller.AuthController
import javafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class DashboardView(authController: AuthController, appState: AppState) {

  private val cardStyle =
    "-fx-background-color: #111827; -fx-border-color: #1e293b; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;"

  appState.currentUser.onChange { (_, _, userOpt) =>
    contentArea.children = userOpt match {
      case Some(user) => Seq(loggedInContent(user.username))
      case None => Seq(loggedOutContent)
    }
  }

  



  private def loggedOutHeader = new VBox {
    spacing = 8
    padding = Insets(20, 40, 24, 40)
    children = Seq(
      new Label("Welcome to ScalaFX Sudoku!") {
        style = "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;"
      },
      new Label("Sign in to access your stats and cloud save.") {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 16px;"
      }
    )
  }

  private def loggedOutPromoCard = new VBox {
    padding = Insets(60)
    spacing = 28
    prefWidth = 560
    minHeight = 360
    style = cardStyle
    alignment = Pos.Center
    children = Seq(
      new Label("Save your progress\nto the cloud") {
        style = "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-alignment: center;"
        alignment = Pos.Center
        maxWidth = Double.MaxValue
      },
      new Label("Sign in to keep track of your progress") {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 18px;"
        wrapText = true
        alignment = Pos.Center
      },
      new HBox {
        spacing = 16
        alignment = Pos.Center
        children = Seq(
          new Button("Sign In / Register") {
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 16 36 16 36;"
            onAction = (_: ActionEvent) => authController.showLoginView()
          },
          new Button("Play as Guest") {
            style = "-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 16 36 16 36;"
          }
        )
      }
    )
  }

  private def loggedOutContent = new VBox {
    children = Seq(
      loggedOutHeader,
      new HBox {
        padding = Insets(180, 32, 24, 32)
        alignment = Pos.Center
        children = Seq(loggedOutPromoCard)
      }
    )
  }


  private def loggedInHeader(username: String) = new VBox {
    spacing = 8
    padding = Insets(40, 40, 24, 40)
    children = Seq(
      new Label(s"Welcome back, $username!") {
        style = "-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;"
      },
      new Label("Your progress is synced with AWS DynamoDB.") {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 14px;"
      }
    )
  }

  private def loggedInCloudSaveCard = new VBox {
    padding = Insets(32)
    spacing = 16
    style = "-fx-background-color: linear-gradient(to bottom right, #1e3a8a, #6d28d9); -fx-border-radius: 16; -fx-background-radius: 16;"
    children = Seq(
      new Label("Your last game was saved. Continue where you left off or start a new one.") {
        style = "-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;"
        wrapText = true
      },
      new Button("▶   Continue Game") {
        style = "-fx-background-color: white; -fx-text-fill: #1e3a8a; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;"
      }
    )
  }

  private def loggedInStatTile(icon: String, label: String, value: String) = new VBox {
    spacing = 6
    padding = Insets(20)
    style = cardStyle
    HBox.setHgrow(this, Priority.Always)
    children = Seq(
      new HBox {
        spacing = 8
        alignment = Pos.CenterLeft
        children = Seq(
          new Label(icon) { style = "-fx-font-size: 18px;" },
          new Label(label) { style = "-fx-text-fill: #9ca3af; -fx-font-size: 11px; -fx-font-weight: bold;" }
        )
      },
      new Label(value) {
        style = "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;"
      }
    )
  }

  private def loggedInStatsSection = new VBox {
    spacing = 12
    padding = Insets(0, 32, 32, 32)
    children = Seq(
      new Label("Profile Stats") {
        style = "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
        padding = Insets(0, 0, 4, 0)
      },
      new HBox {
        spacing = 12
        children = Seq(
          loggedInStatTile("🏆", "PUZZLES SOLVED", "0"),
          loggedInStatTile("🔥", "STREAK (DAYS)",  "0"),
          loggedInStatTile("⏱",  "AVG TIME",        "--:--"),
          loggedInStatTile("📈", "WIN RATE",        "--%")
        )
      }
    )
  }

  private def loggedInContent(username: String) = new VBox {
    children = Seq(
      loggedInHeader(username),
      loggedInStatsSection
    )
  }
  

  private val contentArea = new VBox {
    style = "-fx-background-color: #0B1120;"
    minWidth = 700
    children = Seq(loggedOutContent)
  }


  val view: ScrollPane = new ScrollPane {
    content = contentArea
    fitToWidth = true
    fitToHeight = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style = "-fx-background-color: #0B1120; -fx-background: #0B1120; -fx-border-color: transparent;"
  }
}