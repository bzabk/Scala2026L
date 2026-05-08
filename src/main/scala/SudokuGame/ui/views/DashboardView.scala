package SudokuGame.ui.views

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class DashboardView {

  private val cardStyle =
    "-fx-background-color: #111827; -fx-border-color: #1e293b; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;"

  private val header = new VBox {
    spacing = 8
    padding = Insets(40, 40, 24, 40)
    children = Seq(
      new Label("Welcome to ScalaFX Sudoku! 👋") {
        style = "-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;"
      },
      new Label("Sign in to access your stats and cloud save.") {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 14px;"
      }
    )
  }

  private val loginBtn = new Button("Sign In / Register") {
    style =
      "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 14px; " +
      "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12 20 12 20;"
  }

  private val guestBtn = new Button("Play as Guest") {
    style =
      "-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 14px; " +
      "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12 20 12 20;"
  }

  private val cloudCard = new VBox {
    padding = Insets(40)
    spacing = 20
    style = cardStyle
    alignment = Pos.Center
    maxHeight = Double.MaxValue
    children = Seq(
      new Label("🧠") {
        style = "-fx-font-size: 42px;"
      },
      new Label("Save your progress to the cloud") {
        style = "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;"
      },
      new Label(
        "Sign in or create an account to sync with AWS DynamoDB.\n" +
        "Track your stats and save your game state (Cloud Save) on any device!"
      ) {
        style = "-fx-text-fill: #9ca3af; -fx-font-size: 13px;"
        wrapText = true
        alignment = Pos.Center
      },
      new HBox {
        spacing = 16
        alignment = Pos.Center
        children = Seq(loginBtn, guestBtn)
      }
    )
  }
  HBox.setHgrow(cloudCard, Priority.Always)

  private def levelRow(name: String, textColor: String, bgColor: String): HBox = {
    val spacer = new Region()
    HBox.setHgrow(spacer, Priority.Always)
    new HBox {
      style =
        s"-fx-background-color: $bgColor; -fx-background-radius: 10; " +
        "-fx-padding: 12 16 12 16; -fx-cursor: hand;"
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(name) {
          style = s"-fx-text-fill: $textColor; -fx-font-size: 14px; -fx-font-weight: bold;"
        },
        spacer,
        new Label("▶") {
          style = s"-fx-text-fill: $textColor; -fx-font-size: 11px;"
        }
      )
    }
  }

  private val newGameCard = new VBox {
    padding = Insets(24)
    spacing = 12
    style = cardStyle
    maxHeight = Double.MaxValue
    children = Seq(
      new Label("New Game") {
        style = "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;"
        padding = Insets(0, 0, 4, 0)
      },
      levelRow("Easy",   "#4ade80", "rgba(22, 163, 74, 0.2)"),
      levelRow("Medium", "#fbbf24", "rgba(202, 138, 4, 0.2)"),
      levelRow("Hard",   "#f87171", "rgba(220, 38, 38, 0.2)"),
      levelRow("Expert", "#c084fc", "rgba(147, 51, 234, 0.2)")
    )
  }

  private val cardsRow = new HBox {
    spacing = 20
    padding = Insets(0, 32, 32, 32)
    vgrow = Priority.Always
    children = Seq(cloudCard, newGameCard)
  }
  
  newGameCard.delegate.prefWidthProperty().bind(
    cardsRow.delegate.widthProperty().multiply(0.27)
  )

  private val innerContent = new VBox {
    style = "-fx-background-color: #0B1120;"
    minWidth = 700
    children = Seq(header, cardsRow)
  }
  VBox.setVgrow(cardsRow, Priority.Always)

  val view: ScrollPane = new ScrollPane {
    content = innerContent
    fitToWidth = true
    fitToHeight = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style = "-fx-background-color: #0B1120; -fx-background: #0B1120; -fx-border-color: transparent;"
  }
}