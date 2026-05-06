package SudokuGame.ui.layout
import javafx.event.ActionEvent
import SudokuGame.controller.AuthController
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class SidebarView(authController: AuthController) {

  private val sidebarBgColor = "#1d2a3d"
  private val borderColor = "#324461"
  private val borderLineThickness = "3"
  private val activeNavLinkStyle = "-fx-background-color: rgba(30, 58, 138, 0.5); -fx-background-radius: 8; -fx-text-fill: #60a5fa; -fx-font-size: 15px;"
  private val inactiveNavLinkStyle = "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 15px;"

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
    alignment = Pos.CenterLeft
    children = Seq(
      new Label("🕹") {
        style = "-fx-text-fill: #60a5fa; -fx-font-size: 20px;"
      },
      new Label("ScalaFX Sudoku") {
        style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;"
      }
    )
    style = s"-fx-border-color: $borderColor; -fx-border-width: 0 0 $borderLineThickness 0;"
  }

  private val spacer = new Region {
    vgrow = Priority.Always
  }

  private val iconsVBox = new VBox {
    spacing = 10
    padding = Insets(20, 15, 0, 15)

    private val buttonDashboard = new Button("📊  Dashboard") {
      maxWidth = Double.MaxValue
      alignment = Pos.CenterLeft
      padding = Insets(10, 15, 10, 15)
      style = activeNavLinkStyle
    }

    private val buttonNewGame = new Button("🎮  Nowa Gra") {
      maxWidth = Double.MaxValue
      alignment = Pos.CenterLeft
      padding = Insets(10, 15, 10, 15)
      style = inactiveNavLinkStyle
    }

    children = Seq(buttonDashboard, buttonNewGame)
  }

  private val loginVBoX = new VBox {
    spacing = 10
    padding = Insets(20, 15, 20, 15)
    style = s"-fx-border-color: $borderColor; -fx-border-width:  $borderLineThickness 0 0 0;"
    private val loginButton = new Button("Zaloguj się") {
      maxWidth = Double.MaxValue
      alignment = Pos.Center
      padding = Insets(10, 15, 10, 15)
      style = loginButtonStyle
      onAction = (_: ActionEvent) => authController.showLoginView()
    }

    children = Seq(loginButton)
  }

  val view: VBox = new VBox {
    style = s"-fx-background-color: $sidebarBgColor; -fx-border-color: $borderColor; -fx-border-width: 0 $borderLineThickness 0 0;"
    prefWidth = 260
    children = Seq(logoHBox, iconsVBox,spacer,loginVBoX)
  }
}
