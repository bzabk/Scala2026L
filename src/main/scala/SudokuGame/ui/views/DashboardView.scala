package SudokuGame.ui.views

import SudokuGame.common.{AppState, GameState as SavedGame, GameStatus}
import SudokuGame.controller.{AuthController, SudokuGameController}
import SudokuGame.game.service.GameService
import SudokuGame.model.{GameState as ActiveGameState}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

import java.time.Instant
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

enum Difficulty:
  case Easy, Medium, Hard, Expert

  override def toString(): String = {
    this match
      case Easy   => "Easy"
      case Medium => "Medium"
      case Hard   => "Hard"
      case Expert => "Expert"
  }

class DashboardView(
    authController: AuthController,
    appState: AppState,
    gameService: GameService
) {
  private var _sudokuGameController = new SudokuGameController()

  private val cardStyle =
    "-fx-background-color: #111827; -fx-border-color: #1e293b; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;"

  private var _currentGameView: Option[SudokuBoardView] = None
  private var _activeDifficulty: Difficulty = Difficulty.Easy
  private var _activeGameId: String = ""
  private var _activeCreatedAt: String = ""
  private var _activeSessionToken: Long = 0L
  private var _lastSaveAtMs: Long = 0L
  private var _lastSaveSignature: String = ""
  private var _saveInFlight = false
  private var _pendingSave: Option[(Long, String, SavedGame, String)] = None
  private var _cloudSaveAvailable = true

  private val _saveIntervalMs = 5000L
  private val _syncSuccess = "#4ADE80"
  private val _syncMuted = "#94A3B8"
  private val _syncWarning = "#FBBF24"
  private val _syncError = "#F87171"

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

  appState.recentGames.onChange { (_, _, _) =>
    appState.currentUser.value.foreach { user =>
      if (_currentGameView.isEmpty) {
        contentArea.children = Seq(loggedInContent(user.username))
      }
    }
  }

  private def _currentUsername: Option[String] =
    appState.currentUser.value.map(_.username)

  private def _difficultyFromString(value: String): Difficulty =
    value.toLowerCase match {
      case "medium" => Difficulty.Medium
      case "hard"   => Difficulty.Hard
      case "expert" => Difficulty.Expert
      case _        => Difficulty.Easy
    }

  private def _formatDuration(totalSeconds: Long): String = {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    f"$minutes%02d:$seconds%02d"
  }

  private def _gameDate(game: SavedGame): Option[LocalDate] =
    Try(LocalDate.parse(game.createdAt.take(10))).toOption

  private def _completedGames(games: List[SavedGame]): List[SavedGame] =
    games.filter(_.status == GameStatus.Finished)

  private def _streakDays(games: List[SavedGame]): Int = {
    val days = _completedGames(games)
      .flatMap(_gameDate)
      .distinct
      .sortBy(_.toEpochDay)
      .reverse
    days.headOption match {
      case None => 0
      case Some(latest) =>
        Iterator
          .iterate(latest)(_.minusDays(1))
          .zipWithIndex
          .takeWhile { case (day, _) => days.contains(day) }
          .length
    }
  }

  private def _averageFinishedTime(games: List[SavedGame]): String = {
    val finished = _completedGames(games)
    if (finished.isEmpty) "--:--"
    else _formatDuration(finished.map(_.elapsedSeconds).sum / finished.size)
  }

  private def _winRate(games: List[SavedGame]): String =
    if (games.isEmpty) "--%"
    else {
      val finishedCount = _completedGames(games).size
      s"${finishedCount * 100 / games.size}%"
    }

  private def _prepareActiveGame(
      difficulty: Difficulty,
      savedGame: Option[SavedGame] = None
  ): Unit = {
    _activeDifficulty = difficulty
    _activeCreatedAt = savedGame
      .map(_.createdAt)
      .filter(_.nonEmpty)
      .getOrElse(Instant.now().toString)
    _activeGameId = savedGame
      .flatMap(game => Option(game.gameId).filter(_.nonEmpty))
      .getOrElse(s"${difficulty.toString}-${_activeCreatedAt}")
    _activeSessionToken += 1
    _lastSaveAtMs = 0L
    _lastSaveSignature = ""
    _saveInFlight = false
    _pendingSave = None
    _cloudSaveAvailable = true
  }

  private def _openGameView(difficulty: Difficulty): Unit = {
    _currentGameView = Some(
      new SudokuBoardView(
        _sudokuGameController,
        () => _backToMenu(),
        difficulty,
        state => _handleGameStateChanged(state)
      )
    )

    _currentGameView.foreach { gameView =>
      val (message, color) =
        if _currentUsername.isDefined then ("Ready to sync (AWS)", _syncMuted)
        else ("Guest mode", _syncMuted)
      gameView.setSyncStatus(message, color)
      gameView.startTimer()
      contentArea.children = Seq(gameView.view)
      VBox.setVgrow(gameView.view, Priority.Always)
    }
  }

  private def _setSyncStatus(message: String, color: String): Unit =
    _currentGameView.foreach(_.setSyncStatus(message, color))

  private def _snapshotFor(state: ActiveGameState): SavedGame =
    SavedGame(
      createdAt = _activeCreatedAt,
      difficulty = _activeDifficulty.toString(),
      elapsedSeconds = state.elapsedSeconds.toLong,
      board = state.board.values,
      status = if state.isGameOver then GameStatus.Finished else GameStatus.InProgress,
      gameId = _activeGameId,
      initialBoard = _sudokuGameController.initialBoard,
      notes = state.board.notes,
      errorCount = state.errorCount,
      hintsRemaining = state.hintsRemaining
    )

  private def _snapshotSignature(snapshot: SavedGame): String =
    Seq(
      snapshot.elapsedSeconds,
      snapshot.status,
      snapshot.errorCount,
      snapshot.hintsRemaining,
      snapshot.board,
      snapshot.notes
    ).mkString("|")

  private def _handleGameStateChanged(
      state: ActiveGameState,
      force: Boolean = false
  ): Unit = {
    _currentUsername match {
      case None =>
        _setSyncStatus("Guest mode", _syncMuted)
      case Some(username) if _cloudSaveAvailable =>
        val snapshot = _snapshotFor(state)
        val signature = _snapshotSignature(snapshot)
        val now = System.currentTimeMillis()
        val shouldSave =
          force || state.isGameOver || now - _lastSaveAtMs >= _saveIntervalMs

        if (
          signature != _lastSaveSignature &&
          shouldSave
        ) {
          _scheduleSave(_activeSessionToken, username, snapshot, signature, now)
        }
      case Some(_) =>
        _setSyncStatus("AWS sync unavailable", _syncError)
    }
  }

  private def _scheduleSave(
      sessionToken: Long,
      username: String,
      snapshot: SavedGame,
      signature: String,
      requestedAtMs: Long
  ): Unit = {
    if (_saveInFlight) {
      _pendingSave = Some((sessionToken, username, snapshot, signature))
      _setSyncStatus("Saving latest changes...", _syncWarning)
    } else {
      _startSave(sessionToken, username, snapshot, signature, requestedAtMs)
    }
  }

  private def _startSave(
      sessionToken: Long,
      username: String,
      snapshot: SavedGame,
      signature: String,
      requestedAtMs: Long
  ): Unit = {
    _saveInFlight = true
    _lastSaveAtMs = requestedAtMs
    _setSyncStatus("Saving to AWS...", _syncWarning)

    gameService.saveGame(username, snapshot).onComplete {
      case Success(true) =>
        Platform.runLater {
          _upsertRecentGame(snapshot)
          if (sessionToken == _activeSessionToken) {
            _saveInFlight = false
            _lastSaveSignature = signature
            _flushPendingSave(sessionToken)
          }
        }
      case Success(false) =>
        Platform.runLater {
          if (sessionToken == _activeSessionToken) {
            _saveInFlight = false
            _pendingSave = None
            _cloudSaveAvailable = false
            _setSyncStatus("AWS save route missing", _syncError)
          }
        }
      case Failure(_) =>
        Platform.runLater {
          if (sessionToken == _activeSessionToken) {
            _saveInFlight = false
            _pendingSave = None
            _cloudSaveAvailable = false
            _setSyncStatus("AWS sync failed", _syncError)
          }
        }
    }
  }

  private def _flushPendingSave(sessionToken: Long): Unit = {
    _pendingSave match {
      case Some((pendingSessionToken, username, snapshot, signature))
          if pendingSessionToken == sessionToken &&
            signature != _lastSaveSignature &&
            _cloudSaveAvailable =>
        _pendingSave = None
        _startSave(sessionToken, username, snapshot, signature, System.currentTimeMillis())
      case _ =>
        _pendingSave = None
        _setSyncStatus("Synced (AWS)", _syncSuccess)
    }
  }

  private def _upsertRecentGame(game: SavedGame): Unit = {
    val updated = game :: appState.recentGames.value.filterNot(existing =>
      (game.gameId.nonEmpty && existing.gameId == game.gameId) ||
        (game.gameId.isEmpty && existing.createdAt == game.createdAt)
    )
    appState.recentGames.set(updated.take(6))
  }

  private def _resumeLatestGame(): Unit =
    appState.recentGames.value
      .find(_.status == GameStatus.InProgress)
      .orElse(appState.recentGames.value.headOption)
      .foreach(_resumeSavedGame)

  def resumeSavedGame(game: SavedGame): Unit =
    _resumeSavedGame(game)

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
            delegate.setOnAction(new EventHandler[ActionEvent] {
              override def handle(event: ActionEvent): Unit =
                authController.showLoginView()
            })
          },
          new Button("Play as Guest") {
            style =
              "-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 16 36 16 36;"
            delegate.setOnAction(new EventHandler[ActionEvent] {
              override def handle(event: ActionEvent): Unit =
                contentArea.children = Seq(_buildGuestContent)
            })
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

  private def loggedInCloudSaveCard(games: List[SavedGame]) = new VBox {
    val hasSavedGames = games.nonEmpty
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
      new Button(if (hasSavedGames) "▶   Continue Game" else "No saved games") {
        disable = !hasSavedGames
        opacity = if (hasSavedGames) 1.0 else 0.65
        style =
          "-fx-background-color: white; -fx-text-fill: #1e3a8a; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;"
        delegate.setOnAction(new EventHandler[ActionEvent] {
          override def handle(event: ActionEvent): Unit =
            _resumeLatestGame()
        })
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

  private def loggedInStatsSection(games: List[SavedGame]) = new VBox {
    val solvedCount = _completedGames(games).size.toString
    val streak = _streakDays(games).toString
    val averageTime = _averageFinishedTime(games)
    val winRate = _winRate(games)
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
          loggedInStatTile("🏆", "PUZZLES SOLVED", solvedCount),
          loggedInStatTile("🔥", "STREAK (DAYS)", streak),
          loggedInStatTile("⏱", "AVG TIME", averageTime),
          loggedInStatTile("📈", "WIN RATE", winRate)
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

  private def _startGame(difficulty: Difficulty): Unit = {
    val initialBoard = SudokuPuzzles.boardFor(difficulty)
    _sudokuGameController = new SudokuGameController()
    _prepareActiveGame(difficulty)
    _sudokuGameController.startNewGame(initialBoard)
    _openGameView(difficulty)
    _handleGameStateChanged(_sudokuGameController.gameState, force = true)
  }

  private def _resumeSavedGame(game: SavedGame): Unit = {
    val difficulty = _difficultyFromString(game.difficulty)
    _sudokuGameController = new SudokuGameController()
    _prepareActiveGame(difficulty, Some(game))
    _sudokuGameController.startSavedGame(
      initialBoard = game.resumeInitialBoard,
      boardValues = game.board,
      notes = game.normalizedNotes,
      elapsedSeconds = game.elapsedSeconds.toInt,
      errorCount = game.errorCount,
      hintsRemaining = game.hintsRemaining
    )
    _openGameView(difficulty)
  }

  private def _backToMenu(): Unit = {
    if (_sudokuGameController.gameState != null)
      _handleGameStateChanged(_sudokuGameController.gameState, force = true)
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
        delegate.setOnAction(new EventHandler[ActionEvent] {
          override def handle(event: ActionEvent): Unit =
            contentArea.children = Seq(loggedOutContent)
        })
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
    val games = appState.recentGames.value
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
              children = Seq(loggedInCloudSaveCard(games))
            },
            _buildNewGameCard
          )
        },
        loggedInStatsSection(games)
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
