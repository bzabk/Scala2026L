package SudokuGame.ui.views

import SudokuGame.common.AppState
import SudokuGame.controller.AuthController
import SudokuGame.auth.domain.LoggedUser
import javafx.event.ActionEvent
import SudokuGame.controller.{AuthController, SudokuGameController}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

enum Difficulty:
  case Easy, Medium, Hard, Expert

  override def toString(): String = {
    this match
      case Easy   => "Easy"
      case Medium => "Medium"
      case Hard   => "Hard"
      case Expert => "Expert"
  }

class DashboardView(authController: AuthController, appState: AppState) {
  private val _sudokuGameController = new SudokuGameController()

  private val cardStyle =
    "-fx-background-color: #111827; -fx-border-color: #1e293b; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;"

  private var _currentGameView: Option[SudokuBoardView] = None

  private val contentArea = new VBox {
    style = "-fx-background-color: #0B1120;"
    minWidth = 700
    children = Seq()
  }

  appState.currentUser.onChange { (_, _, userOpt) =>
    contentArea.children = userOpt match {
      case Some(user) => Seq(loggedInContent(user.username))
      case None       => Seq(loggedOutContent)
    }
  }
  private def loggedOutHeader = new VBox {
    spacing = 8
    padding = Insets(20, 40, 24, 40)
    children = Seq(
      new Label("Welcome to ScalaFX Sudoku!") {
        style =
          "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;"
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
        style =
          "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-alignment: center;"
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
            style =
              "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 16 36 16 36;"
            onAction = (_: ActionEvent) => authController.showLoginView()
          },
          new Button("Play as Guest") {
            style =
              "-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 16 36 16 36;"
            onAction =
              (_: ActionEvent) => contentArea.children = Seq(_buildGuestContent)
          }
        )
      }
    )
  }

  private def loggedOutContent = new VBox {
    children = Seq(
      loggedOutHeader,
      new HBox {
        spacing = 24
        padding = Insets(24, 40, 24, 40)
        children = Seq(
          new VBox {
            HBox.setHgrow(this, Priority.Always)
            children = Seq(loggedOutPromoCard)
          },
          _buildNewGameCard
        )
      }
    )
  }

  private def loggedInHeader(username: String) = new VBox {
    spacing = 8
    padding = Insets(40, 40, 24, 40)
    children = Seq(
      new Label(s"Welcome back, $username!") {
        style =
          "-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;"
      },
      new Label("Your progress is synced with AWS DynamoDB.") {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 14px;"
      }
    )
  }

  private def loggedInCloudSaveCard = new VBox {
    padding = Insets(32)
    spacing = 16
    style =
      "-fx-background-color: linear-gradient(to bottom right, #1e3a8a, #6d28d9); -fx-border-radius: 16; -fx-background-radius: 16;"
    children = Seq(
      new Label(
        "Your last game was saved. Continue where you left off or start a new one."
      ) {
        style = "-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;"
        wrapText = true
      },
      new Button("▶   Continue Game") {
        style =
          "-fx-background-color: white; -fx-text-fill: #1e3a8a; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;"
      }
    )
  }

  private def loggedInStatTile(icon: String, label: String, value: String) =
    new VBox {
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
            new Label(label) {
              style =
                "-fx-text-fill: #9ca3af; -fx-font-size: 11px; -fx-font-weight: bold;"
            }
          )
        },
        new Label(value) {
          style =
            "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;"
        }
      )
    }

  private def loggedInStatsSection = new VBox {
    spacing = 12
    padding = Insets(0, 32, 32, 32)
    children = Seq(
      new Label("Profile Stats") {
        style =
          "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
        padding = Insets(0, 0, 4, 0)
      },
      new HBox {
        spacing = 12
        children = Seq(
          loggedInStatTile("🏆", "PUZZLES SOLVED", "0"),
          loggedInStatTile("🔥", "STREAK (DAYS)", "0"),
          loggedInStatTile("⏱", "AVG TIME", "--:--"),
          loggedInStatTile("📈", "WIN RATE", "--%")
        )
      }
    )
  }

  private def _levelRow(
      difficulty: Difficulty,
      textColor: String,
      bgColor: String
  ): HBox = {
    val spacer = new Region()
    HBox.setHgrow(spacer, Priority.Always)
    val row = new HBox {
      maxWidth = Double.MaxValue
      style = s"-fx-background-color: $bgColor; -fx-background-radius: 10; " +
        "-fx-padding: 12 16 12 16; -fx-cursor: hand;"
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(difficulty.toString()) {
          style =
            s"-fx-text-fill: $textColor; -fx-font-size: 14px; -fx-font-weight: bold;"
        },
        spacer,
        new Label("▶") {
          style = s"-fx-text-fill: $textColor; -fx-font-size: 11px;"
        }
      )
    }
    row.delegate.setOnMouseClicked(new EventHandler[MouseEvent] {
      override def handle(e: MouseEvent): Unit = _startGame(difficulty)
    })
    row
  }

  // TODO(anyone): Implement generator or store many examples and load them
  private def _startGame(difficulty: Difficulty): Unit = {
    val boards = Map(
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
      Difficulty.Hard -> Array(
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
      Difficulty.Expert -> Array(
        Array(5, 3, 0, 0, 7, 0, 0, 0, 0),
        Array(6, 0, 0, 1, 9, 5, 0, 0, 0),
        Array(0, 9, 8, 0, 0, 0, 0, 6, 0),
        Array(8, 0, 0, 0, 6, 0, 0, 0, 3),
        Array(4, 0, 0, 8, 0, 3, 0, 0, 1),
        Array(7, 0, 0, 0, 2, 0, 0, 0, 6),
        Array(0, 6, 0, 0, 0, 0, 2, 8, 0),
        Array(0, 0, 0, 4, 1, 9, 0, 0, 5),
        Array(0, 0, 0, 0, 8, 0, 0, 7, 9)
      )
    )

    val initialBoard = boards.getOrElse(difficulty, boards(Difficulty.Easy))
    _sudokuGameController.startNewGame(initialBoard)

    _currentGameView = Some(
      new SudokuBoardView(
        _sudokuGameController,
        () => _backToMenu(),
        difficulty
      )
    )
    _currentGameView.get.startTimer()

    contentArea.children = Seq(_currentGameView.get.view)
    VBox.setVgrow(_currentGameView.get.view, Priority.Always)
  }

  private def _backToMenu(): Unit = {
    _currentGameView.foreach(_.stop())
    _currentGameView = None
    appState.currentUser.value match {
      case Some(user) =>
        contentArea.children = Seq(loggedInContent(user.username))
      case None => contentArea.children = Seq(_buildGuestContent)
    }
  }

  private def _buildMenuHeader = new HBox {
    padding = Insets(20, 32, 20, 32)
    alignment = Pos.CenterLeft
    children = Seq(
      new Button("← Back") {
        style =
          "-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;"
        onAction =
          (_: ActionEvent) => contentArea.children = Seq(loggedOutContent)
      }
    )
  }

  private def _buildNewGameCard = new VBox {
    padding = Insets(24)
    spacing = 12
    prefWidth = 320
    style = cardStyle
    children = Seq(
      new Label("New Game") {
        style =
          "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;"
        padding = Insets(0, 0, 4, 0)
      },
      _levelRow(Difficulty.Easy, "#4ade80", "rgba(22, 163, 74, 0.2)"),
      _levelRow(Difficulty.Medium, "#fbbf24", "rgba(202, 138, 4, 0.2)"),
      _levelRow(Difficulty.Hard, "#f87171", "rgba(220, 38, 38, 0.2)"),
      _levelRow(Difficulty.Expert, "#c084fc", "rgba(147, 51, 234, 0.2)")
    )
  }

  private def loggedInContent(username: String): VBox = {
    new VBox {
      style = "-fx-background-color: #0B1120;"
      minWidth = 700
      children = Seq(
        _buildMenuHeader,
        loggedInHeader(username),
        new HBox {
          spacing = 24
          padding = Insets(24, 40, 24, 40)
          children = Seq(
            new VBox {
              HBox.setHgrow(this, Priority.Always)
              children = Seq(loggedInCloudSaveCard)
            },
            _buildNewGameCard
          )
        },
        loggedInStatsSection
      )
    }
  }

  private def _buildGuestContent: VBox = {
    new VBox {
      style = "-fx-background-color: #0B1120;"
      minWidth = 700
      children = Seq(
        _buildMenuHeader,
        new HBox {
          spacing = 24
          padding = Insets(24, 40, 24, 40)
          children = Seq(
            new VBox {
              HBox.setHgrow(this, Priority.Always)
              children = Seq(loggedOutPromoCard)
            },
            _buildNewGameCard
          )
        }
      )
    }
  }

  // Initialize contentArea with the initial content
  contentArea.children = Seq(loggedOutContent)

  val view: ScrollPane = new ScrollPane {
    content = contentArea
    fitToWidth = true
    fitToHeight = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style =
      "-fx-background-color: #0B1120; -fx-background: #0B1120; -fx-border-color: transparent;"
  }
}
