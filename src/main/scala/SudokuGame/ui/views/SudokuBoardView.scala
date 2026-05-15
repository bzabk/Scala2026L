package SudokuGame.ui.views

import SudokuGame.controller.SudokuGameController
import SudokuGame.model.GameState
import scalafx.Includes.{jfxKeyEvent2sfx, jfxScene2sfx}
import scalafx.animation.{Animation, KeyFrame, Timeline}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ContentDisplay, Label}
import scalafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import scalafx.scene.layout.{GridPane, HBox, Priority, Region, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.SVGPath
import scalafx.util.Duration

import scala.collection.mutable as mutable
import scala.collection.immutable as immutable

class SudokuBoardView(
    gameController: SudokuGameController,
    onBack: () => Unit,
    difficulty: Difficulty
) {

  private val _fontFamily =
    "\"Avenir Next\", \"Helvetica Neue\", \"Segoe UI\", sans-serif"
  private val _surfaceBg = "#0F172A"
  private val _boardBg = "#1E293B"
  private val _givenCellBg = "#0F172A"
  private val _panelBg = "#1E293B"
  private val _gridLine = "#94A3B8"
  private val _textMain = "#F8FAFC"
  private val _textMuted = "#94A3B8"
  private val _playerNumber = "#60A5FA"
  private val _success = "#4ADE80"
  private val _danger = "#EF4444"
  private val _boardSize = 9
  private val _boxSize = 3
  private val _cellSize = 52

  private val _cellViews: mutable.Map[(Int, Int), (StackPane, Label)] =
    mutable.Map.empty
  private val _timerTimeline = new Timeline {
    keyFrames = Seq(
      KeyFrame(
        Duration(1000),
        onFinished = _ => {
          gameController.updateTime()
          if (
            gameController.gameState != null && !gameController.gameState.isGameOver
          )
            playFromStart()
        }
      )
    )
    cycleCount = Animation.Indefinite
  }

  private def _setHistoryButtonState(button: Button, enabled: Boolean): Unit = {
    button.disable = !enabled
    button.opacity = if (enabled) 1.0 else 0.55
  }

  private def _styleButton(
      bg: String,
      text: String,
      border: String,
      size: Int
  ): String =
    s"-fx-background-color: $bg; -fx-text-fill: $text; -fx-font-size: ${size}px; -fx-font-weight: 800; -fx-font-family: $_fontFamily; -fx-background-radius: 14; -fx-border-color: $border; -fx-border-radius: 14; -fx-background-insets: 0; -fx-border-insets: 0; -fx-padding: 0; -fx-cursor: hand;"

  private def _icon(path: String): SVGPath =
    new SVGPath {
      content = path
      fill = Color.Transparent
      stroke = Color.web(_textMain)
      strokeWidth = 2
      scaleX = 1.15
      scaleY = 1.15
    }

  private def _cellBorderWidth(row: Int, col: Int): String = {
    val top = if (row == 0 || row % _boxSize == 0) 2 else 1
    val right = if (col == _boardSize - 1 || (col + 1) % _boxSize == 0) 2 else 1
    val bottom =
      if (row == _boardSize - 1 || (row + 1) % _boxSize == 0) 2 else 1
    val left = if (col == 0 || col % _boxSize == 0) 2 else 1
    s"$top $right $bottom $left"
  }

  private def _cellStyle(row: Int, col: Int, background: String): String =
    s"-fx-background-color: $background; -fx-border-color: $_gridLine; -fx-border-width: ${_cellBorderWidth(row, col)}; -fx-background-insets: 0; -fx-border-insets: 0; -fx-cursor: hand;"

  private def _labelStyle(textColor: String, fontWeight: Int): String =
    s"-fx-text-fill: $textColor; -fx-font-size: 28px; -fx-font-weight: $fontWeight; -fx-font-family: $_fontFamily;"

  private def _notesGridStyle(textColor: String): String =
    s"-fx-text-fill: $textColor; -fx-font-size: 10px; -fx-font-weight: 600; -fx-font-family: $_fontFamily;"

  private def _focusBoard(): Unit =
    view.requestFocus()

  private def _togglePause(): Unit = {
    gameController.togglePause()
    _focusBoard()
  }

  private def _setTimerRunning(running: Boolean): Unit = {
    if (running) {
      if (_timerTimeline.status != Animation.Status.Running) {
        _timerTimeline.playFromStart()
      }
    } else {
      _timerTimeline.stop()
    }
  }

  private def _lucideIcon(
      path: String,
      color: String,
      scale: Double = 1.0
  ): SVGPath =
    new SVGPath {
      content = path
      fill = Color.Transparent
      stroke = Color.web(color)
      strokeWidth = 2
      strokeLineCap = scalafx.scene.shape.StrokeLineCap.Round
      strokeLineJoin = scalafx.scene.shape.StrokeLineJoin.Round
      scaleX = scale
      scaleY = scale
    }

  private def _roundedRectPath(
      x: Double,
      y: Double,
      width: Double,
      height: Double,
      radius: Double = 1.0
  ): String =
    s"M${x + radius} $y H${x + width - radius} A$radius $radius 0 0 1 ${x + width} ${y + radius} V${y + height - radius} A$radius $radius 0 0 1 ${x + width - radius} ${y + height} H${x + radius} A$radius $radius 0 0 1 $x ${y + height - radius} V${y + radius} A$radius $radius 0 0 1 ${x + radius} $y Z"

  private def _pauseButtonGraphic(isPaused: Boolean): SVGPath =
    if (isPaused)
      _lucideIcon("M6 3L20 12L6 21Z", _textMain, scale = 0.95)
    else
      _lucideIcon(
        s"${_roundedRectPath(14, 4, 4, 16)} ${_roundedRectPath(6, 4, 4, 16)}",
        _textMain,
        scale = 0.95
      )

  private def _pausedOverlayGraphic(): SVGPath =
    _lucideIcon(
      s"${_roundedRectPath(14, 4, 4, 16)} ${_roundedRectPath(6, 4, 4, 16)}",
      "#2F7BFF",
      scale = 1.75
    )

  private def _gameOverGraphic(isSuccess: Boolean): SVGPath =
    if (isSuccess)
      _lucideIcon("M20 6 9 17l-5-5", "#4ADE80", scale = 1.9)
    else
      _lucideIcon("M18 6 6 18 M6 6l12 12", "#EF4444", scale = 1.9)

  private def _notesButtonGraphic(): SVGPath =
    _lucideIcon(
      "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z m-6.174-1.812 4 4",
      _textMain,
      scale = 0.9
    )

  private val _pauseButton = new Button("▌▌") {
    prefWidth = 46
    prefHeight = 46
    style =
      s"-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: $_textMain; -fx-font-size: 18px; -fx-font-weight: 900; -fx-font-family: $_fontFamily; -fx-background-radius: 12; -fx-cursor: hand;"
    graphic = _pauseButtonGraphic(isPaused = false)
    text = ""
    contentDisplay = ContentDisplay.GraphicOnly
    onAction = _ => _togglePause()
  }

  private val _pausedTitle = new Label("Game paused") {
    style =
      s"-fx-text-fill: $_textMain; -fx-font-size: 32px; -fx-font-weight: 900; -fx-font-family: $_fontFamily;"
  }

  private val _pausedIcon = new StackPane {
    alignment = Pos.Center
    children = Seq(_pausedOverlayGraphic())
  }

  private val _continueButton = new Button("Continue game") {
    prefWidth = 190
    prefHeight = 54
    style = _styleButton("#2563EB", _textMain, "#2563EB", 15)
    onAction = _ => _togglePause()
  }

  private val _pausedPanel = new VBox {
    spacing = 20
    alignment = Pos.Center
    padding = Insets(16)
    prefWidth = _cellSize * _boardSize + 8
    prefHeight = _cellSize * _boardSize + 8
    maxWidth = _cellSize * _boardSize + 8
    maxHeight = _cellSize * _boardSize + 8
    style =
      s"-fx-background-color: #0B1A45; -fx-border-color: #223A61; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;"
    children = Seq(_pausedIcon, _pausedTitle, _continueButton)
  }

  private val _gameOverTitle = new Label("Puzzle solved") {
    style =
      s"-fx-text-fill: $_textMain; -fx-font-size: 32px; -fx-font-weight: 900; -fx-font-family: $_fontFamily;"
  }

  private val _gameOverMessage = new Label("You completed the puzzle.") {
    style =
      s"-fx-text-fill: $_textMuted; -fx-font-size: 15px; -fx-font-weight: 700; -fx-font-family: $_fontFamily;"
  }

  private val _gameOverButton = new Button("Try again") {
    prefWidth = 190
    prefHeight = 54
    style = _styleButton("#2563EB", _textMain, "#2563EB", 15)
    onAction = _ => gameController.restartGame()
  }

  private val _gameOverIcon = new StackPane {
    alignment = Pos.Center
  }

  private val _gameOverPanel = new VBox {
    spacing = 20
    alignment = Pos.Center
    padding = Insets(16)
    prefWidth = _cellSize * _boardSize + 8
    prefHeight = _cellSize * _boardSize + 8
    maxWidth = _cellSize * _boardSize + 8
    maxHeight = _cellSize * _boardSize + 8
    style =
      s"-fx-background-color: #102A1E; -fx-border-color: #22C55E; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;"
    children =
      Seq(_gameOverIcon, _gameOverTitle, _gameOverMessage, _gameOverButton)
  }

  private def _handleKeyPress(keyCode: KeyCode): Unit = {
    if (keyCode == KeyCode.P || keyCode == KeyCode.ESCAPE) {
      gameController.togglePause()
      _focusBoard()
      return
    }

    if (keyCode == KeyCode.N) {
      gameController.toggleNotesMode()
      _focusBoard()
      return
    }

    if (gameController.gameState != null && gameController.gameState.isPaused)
      return

    keyCode match {
      case KeyCode.LEFT =>
        gameController.moveSelection(0, -1)
        _focusBoard()
      case KeyCode.RIGHT =>
        gameController.moveSelection(0, 1)
        _focusBoard()
      case KeyCode.UP =>
        gameController.moveSelection(-1, 0)
        _focusBoard()
      case KeyCode.DOWN =>
        gameController.moveSelection(1, 0)
        _focusBoard()
      case KeyCode.HOME =>
        gameController.moveSelection(0, -8)
        _focusBoard()
      case KeyCode.END =>
        gameController.moveSelection(0, 8)
        _focusBoard()
      case KeyCode.PAGE_UP =>
        gameController.moveSelection(-8, 0)
        _focusBoard()
      case KeyCode.PAGE_DOWN =>
        gameController.moveSelection(8, 0)
        _focusBoard()
      case _ =>
    }

    val digit = keyCode match {
      case KeyCode.DIGIT1 | KeyCode.NUMPAD1 => Some(1)
      case KeyCode.DIGIT2 | KeyCode.NUMPAD2 => Some(2)
      case KeyCode.DIGIT3 | KeyCode.NUMPAD3 => Some(3)
      case KeyCode.DIGIT4 | KeyCode.NUMPAD4 => Some(4)
      case KeyCode.DIGIT5 | KeyCode.NUMPAD5 => Some(5)
      case KeyCode.DIGIT6 | KeyCode.NUMPAD6 => Some(6)
      case KeyCode.DIGIT7 | KeyCode.NUMPAD7 => Some(7)
      case KeyCode.DIGIT8 | KeyCode.NUMPAD8 => Some(8)
      case KeyCode.DIGIT9 | KeyCode.NUMPAD9 => Some(9)
      case _                                => None
    }

    digit match {
      case Some(value) => gameController.placeNumber(value)
      case None if keyCode == KeyCode.BACK_SPACE || keyCode == KeyCode.DELETE =>
        gameController.clearCell()
      case None if keyCode == KeyCode.SPACE =>
        _focusBoard()
      case _ => ()
    }
  }

  private def _createNotesGrid(
      notes: immutable.SortedSet[Int],
      conflicts: mutable.Set[Int]
  ): GridPane = {
    val grid = new GridPane {
      hgap = 1
      vgap = 1
      padding = Insets(2)
      prefWidth = _cellSize
      prefHeight = _cellSize
    }

    for (num <- 1 to 9) {
      val row = (num - 1) / 3
      val col = (num - 1) % 3

      val label = new Label(if (notes.contains(num)) num.toString else "") {
        mouseTransparent = true
        minWidth = 14
        minHeight = 14
        prefWidth = 14
        prefHeight = 14
        alignment = Pos.Center
        val textColor =
          if (notes.contains(num) && conflicts.contains(num)) "#FCA5A5"
          else "#94A3B8"
        style = _notesGridStyle(textColor)
      }

      grid.add(label, col, row)
    }

    grid
  }

  private def _createCell(row: Int, col: Int): StackPane = {
    val label = new Label("") {
      mouseTransparent = true
      style = _labelStyle(_textMain, 800)
    }

    val cell = new StackPane {
      prefWidth = _cellSize
      prefHeight = _cellSize
      minWidth = _cellSize
      minHeight = _cellSize
      maxWidth = _cellSize
      maxHeight = _cellSize
      alignment = Pos.Center
      children = Seq(label)
      onMouseClicked = _ => {
        gameController.selectCell(row, col)
        _focusBoard()
      }
      style = _cellStyle(row, col, _boardBg)
    }

    _cellViews((row, col)) = (cell, label)
    cell
  }

  private def _updateCellDisplay(
      cellPane: StackPane,
      label: Label,
      row: Int,
      col: Int,
      gameState: GameState
  ): Unit = {
    val cell = gameState.board.getCell(row, col)
    val selected =
      gameState.selectedRow.contains(row) && gameState.selectedCol.contains(col)
    val conflicts = gameState.conflicts(row)(col)

    val related =
      gameState.selectedRow.contains(row) ||
        gameState.selectedCol.contains(col) ||
        (gameState.selectedRow
          .exists(selectedRow => selectedRow / _boxSize == row / _boxSize) &&
          gameState.selectedCol.exists(selectedCol =>
            selectedCol / _boxSize == col / _boxSize
          ))

    val selectedValue = for {
      selectedRow <- gameState.selectedRow
      selectedCol <- gameState.selectedCol
    } yield gameState.board.getCellValue(selectedRow, selectedCol)

    val sameValue =
      selectedValue.exists(value => value != 0 && cell.value == value)

    val background =
      if (!conflicts.isEmpty && cell.value != 0) "#7F1D1D"
      else if (selected) "#1D4ED8"
      else if (related) "#334155"
      else if (sameValue) "#1E3A8A"
      else if (cell.isGiven) _givenCellBg
      else _boardBg

    if (cell.notes.nonEmpty) {
      val notesGrid = _createNotesGrid(cell.notes, conflicts)
      cellPane.children = Seq(notesGrid)
    } else {
      val textColor =
        if (conflicts.contains(cell.value)) "#FCA5A5"
        else if (selected) _textMain
        else if (cell.isGiven) _textMain
        else _playerNumber

      label.text = if (cell.value == 0) "" else cell.value.toString
      label.style = _labelStyle(textColor, if (cell.isGiven) 800 else 600)
      cellPane.children = Seq(label)
    }

    cellPane.style = _cellStyle(row, col, background)
  }

  private val _boardGrid = new GridPane {
    hgap = 0
    vgap = 0
    padding = Insets(0)
    style =
      s"-fx-background-color: $_gridLine; -fx-border-color: $_gridLine; -fx-border-width: 4; -fx-background-radius: 6; -fx-border-radius: 6;"

    for {
      row <- 0 until _boardSize
      col <- 0 until _boardSize
    } add(_createCell(row, col), col, row)
  }

  private val _boardArea = new StackPane {
    alignment = Pos.Center
    prefWidth = _cellSize * _boardSize + 8
    prefHeight = _cellSize * _boardSize + 8
    minWidth = _cellSize * _boardSize + 8
    minHeight = _cellSize * _boardSize + 8
    maxWidth = _cellSize * _boardSize + 8
    maxHeight = _cellSize * _boardSize + 8
    children = Seq(_boardGrid, _pausedPanel, _gameOverPanel)
  }

  StackPane.setAlignment(_pausedPanel, Pos.Center)
  StackPane.setAlignment(_boardGrid, Pos.Center)
  StackPane.setAlignment(_gameOverPanel, Pos.Center)

  private val _backButton = new Button("←  Back") {
    style =
      s"-fx-background-color: transparent; -fx-text-fill: $_textMuted; -fx-font-size: 15px; -fx-font-weight: 700; -fx-font-family: $_fontFamily; -fx-cursor: hand;"
    onAction = _ => onBack()
  }

  private val _difficultyLabel = new Label(
    difficulty.toString().toUpperCase()
  ) {
    style =
      s"-fx-text-fill: $_textMuted; -fx-font-size: 13px; -fx-font-weight: 800; -fx-font-family: $_fontFamily; -fx-letter-spacing: 1px;"
  }

  private val _syncLabel = new Label("✓  Synced (AWS)") {
    style =
      s"-fx-text-fill: $_success; -fx-font-size: 13px; -fx-font-weight: 800; -fx-font-family: $_fontFamily;"
  }

  private val _timerLabel = new Label("00:00") {
    style =
      s"-fx-text-fill: $_textMain; -fx-font-size: 29px; -fx-font-weight: 800; -fx-font-family: $_fontFamily;"
  }

  private val _errorLabel = new Label("Errors: 0/3") {
    style =
      s"-fx-text-fill: $_danger; -fx-font-size: 15px; -fx-font-weight: 800; -fx-font-family: $_fontFamily;"
  }

  private val _timerPill = new HBox {
    spacing = 10
    alignment = Pos.Center
    padding = Insets(10, 16, 10, 16)
    style = "-fx-background-color: #243149; -fx-background-radius: 14;"
    children = Seq(
      _timerLabel,
      _pauseButton
    )
  }

  private val _topBar = new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(8, 0, 30, 0)
    children = Seq(
      _backButton,
      new Region { HBox.setHgrow(this, Priority.Always) },
      new VBox {
        spacing = 4; alignment = Pos.CenterRight;
        children = Seq(_difficultyLabel, _syncLabel)
      }
    )
  }

  private val _statusRow = new HBox {
    alignment = Pos.Center
    padding = Insets(0, 2, 14, 2)
    children = Seq(
      _errorLabel,
      new Region { HBox.setHgrow(this, Priority.Always) },
      _timerPill
    )
  }

  private val _undoIcon =
    "M3 7v6h6 M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13"
  private val _redoIcon =
    "M21 7v6h-6 M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3L21 13"
  private val _deleteIcon =
    "M3 6h18 M8 6V4h8v2 M19 6l-1 14H6L5 6 M10 11v6 M14 11v6"
  private val _helpIcon =
    "M9.09 9a3 3 0 1 1 5.83 1c0 2-3 2-3 4 M12 17h.01 M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0"

  private def controlButton(
      label: String,
      iconPath: String,
      prefWidthValue: Double = 76
  ): Button =
    new Button(label) {
      prefWidth = prefWidthValue
      prefHeight = 70
      graphic = _icon(iconPath)
      contentDisplay = ContentDisplay.Top
      graphicTextGap = 8
      style = _styleButton(_panelBg, _textMain, "#324663", 13)
    }

  private val undoBtn = controlButton("Undo", _undoIcon)
  private val redoBtn = controlButton("Redo", _redoIcon)
  private val deleteBtn = controlButton("Delete", _deleteIcon)
  private val helpBtn = controlButton("Hint", _helpIcon)

  _setHistoryButtonState(undoBtn, enabled = false)
  _setHistoryButtonState(redoBtn, enabled = false)
  undoBtn.onAction = _ => gameController.undo()
  redoBtn.onAction = _ => gameController.redo()
  deleteBtn.onAction = _ => gameController.clearCell()

  private val helpBadge = new StackPane {
    children = Seq(
      helpBtn,
      new Label("3") {
        style =
          s"-fx-background-color: #2E6CFF; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 800; -fx-font-family: $_fontFamily; -fx-background-radius: 999; -fx-padding: 1 5 1 5;"
        StackPane.setAlignment(this, Pos.TopRight)
        translateX = -6
        translateY = -6
      }
    )
  }

  private val actionBar = new HBox {
    spacing = 12
    alignment = Pos.Center
    children = Seq(undoBtn, redoBtn, deleteBtn, helpBadge)
  }

  private val notesButton = new Button("Notes Mode OFF") {
    prefWidth = 336
    prefHeight = 58
    style = _styleButton(_panelBg, _textMain, "#324663", 16)
    graphic = _notesButtonGraphic()
    contentDisplay = ContentDisplay.Left
    graphicTextGap = 10
    onAction = _ => gameController.toggleNotesMode()
  }

  private def numberButton(number: Int): Button = new Button(number.toString) {
    prefWidth = 104
    prefHeight = 72
    style = _styleButton("#223049", _textMain, "#324663", 24)
    onAction = _ => gameController.placeNumber(number)
  }

  private val numberPad = new VBox {
    spacing = 14
    children = Seq(
      new HBox {
        spacing = 10;
        children = Seq(numberButton(1), numberButton(2), numberButton(3))
      },
      new HBox {
        spacing = 10;
        children = Seq(numberButton(4), numberButton(5), numberButton(6))
      },
      new HBox {
        spacing = 10;
        children = Seq(numberButton(7), numberButton(8), numberButton(9))
      }
    )
  }

  private val controlsPanel = new VBox {
    spacing = 22
    alignment = Pos.TopCenter
    padding = Insets(58, 0, 0, 0)
    maxWidth = 350
    children = Seq(actionBar, notesButton, numberPad)
  }

  private val boardColumn = new VBox {
    alignment = Pos.TopCenter
    maxWidth = 500
    children = Seq(_statusRow, _boardArea)
  }

  private val contentRow = new HBox {
    spacing = 48
    alignment = Pos.TopCenter
    children = Seq(boardColumn, controlsPanel)
  }

  private val _mainArea = new StackPane {
    children = Seq(contentRow)
  }

  private val gameContent = new VBox {
    padding = Insets(28, 24, 30, 24)
    maxWidth = 980
    children = Seq(_topBar, _mainArea)
  }

  val view = new VBox {
    spacing = 0
    style = s"-fx-background-color: $_surfaceBg;"
    alignment = Pos.TopCenter
    focusTraversable = true
    onMouseClicked = _ => _focusBoard()
    children = Seq(gameContent)
  }

  view.scene.onChange { (_, _, newScene) =>
    if (newScene != null) {
      newScene.delegate.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event => _handleKeyPress(event.code)
      )
    }
  }

  private def updateBoardDisplay(gameState: GameState): Unit = {
    _timerLabel.text = gameState.formatTime()
    _errorLabel.text = s"Errors: ${gameState.errorCount}/${gameState.maxErrors}"
    _setHistoryButtonState(undoBtn, gameState.canUndo)
    _setHistoryButtonState(redoBtn, gameState.canRedo)
    _setTimerRunning(!gameState.isPaused && !gameState.isGameOver)

    val gameIsOver = gameState.isGameOver
    val gameWasWon = gameState.isSolved
    val gameWasLost = gameState.isLost

    _pausedPanel.visible = gameState.isPaused
    _pausedPanel.managed = gameState.isPaused
    _gameOverPanel.visible = gameIsOver
    _gameOverPanel.managed = gameIsOver
    _boardGrid.visible = !gameState.isPaused && !gameIsOver
    _boardGrid.managed = !gameState.isPaused && !gameIsOver
    _pauseButton.graphic = _pauseButtonGraphic(gameState.isPaused)
    notesButton.text =
      if (gameState.isNotesMode) "Notes Mode ON" else "Notes Mode OFF"

    if (gameWasWon) {
      _gameOverPanel.style =
        s"-fx-background-color: #102A1E; -fx-border-color: #22C55E; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;"
      _gameOverTitle.text = "Puzzle solved"
      _gameOverMessage.text = s"Finished in ${gameState.formatTime()}"
      _gameOverIcon.children = Seq(_gameOverGraphic(isSuccess = true))
    } else if (gameWasLost) {
      _gameOverPanel.style =
        s"-fx-background-color: #2A1111; -fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;"
      _gameOverTitle.text = "Game over"
      _gameOverMessage.text = "Too many errors. Try again."
      _gameOverIcon.children = Seq(_gameOverGraphic(isSuccess = false))
    }

    for {
      row <- 0 until _boardSize
      col <- 0 until _boardSize
    } _cellViews
      .get((row, col))
      .foreach { case (cellPane, label) =>
        _updateCellDisplay(cellPane, label, row, col, gameState)
      }
  }

  private val _gameStateSubscription: Unit =
    gameController.gameStateProperty.onChange { (_, _, newState) =>
      if (newState != null) updateBoardDisplay(newState)
    }

  if (gameController.gameState != null)
    updateBoardDisplay(gameController.gameState)

  def startTimer(): Unit = {
    _setTimerRunning(true)
  }

  def stop(): Unit = _setTimerRunning(false)
}
