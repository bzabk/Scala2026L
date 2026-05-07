package SudokuGame.auth.ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField, TextField}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

class RegisterView(
  onClose:         () => Unit                       = () => {},
  onRegister:      (String, String, String) => Unit = (_, _, _) => {},
  onSwitchToLogin: () => Unit                       = () => {}
) {

  private val modalBgColor = "#1e2433"
  private val inputBgColor = "#0f172a"
  private val borderColor  = "#2a364a"
  private val textMuted    = "#9ca3af"

  private val labelStyle = s"-fx-text-fill: $textMuted; -fx-font-size: 13px; -fx-font-weight: bold;"

  private val inputStyle = s"""
    -fx-background-color: $inputBgColor;
    -fx-border-color: $borderColor;
    -fx-border-width: 1;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-text-fill: white;
    -fx-prompt-text-fill: #475569;
    -fx-padding: 12 15 12 15;
    -fx-font-size: 14px;
  """

  private val primaryBtnStyle = """
    -fx-background-color: #2563EB;
    -fx-text-fill: white;
    -fx-font-size: 15px;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-cursor: hand;
  """

  private val secondaryBtnStyle = """
    -fx-background-color: rgba(37, 99, 235, 0.15);
    -fx-text-fill: #60a5fa;
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-cursor: hand;
  """

  private val header = new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(0, 0, 15, 0)
    children = Seq(
      new Label("Rejestracja w AWS Cognito") {
        style = "-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;"
      },
      new Region { hgrow = Priority.Always },
      new Button("✕") {
        style = "-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand;"
        onAction       = _ => onClose()
        onMouseEntered = _ => style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;"
        onMouseExited  = _ => style = "-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand;"
      }
    )
  }

  private def separator(): Region = new Region {
    prefHeight = 1
    style = s"-fx-background-color: $borderColor;"
  }

  private val emailField = new TextField {
    style = inputStyle
  }

  private val usernameField = new TextField {
    promptText = "Wprowadź login"
    style = inputStyle
  }

  private val passwordField = new PasswordField {
    promptText = "••••••••"
    style = inputStyle
  }

  private val errorLabel = new Label("") {
    style = "-fx-text-fill: #f87171; -fx-font-size: 12px;"
    visible = false
    managed <== visible
  }

  private val registerBtn = new Button("Zarejestruj się") {
    maxWidth = Double.MaxValue
    padding = Insets(12, 0, 12, 0)
    style = primaryBtnStyle
    onAction = _ => {
      errorLabel.visible = false
      onRegister(emailField.text.value, usernameField.text.value, passwordField.text.value)
    }
    onMouseEntered = _ => style = primaryBtnStyle + "-fx-background-color: #1d4ed8;"
    onMouseExited  = _ => style = primaryBtnStyle
  }

  private val formBox = new VBox {
    spacing = 18
    padding = Insets(20, 0, 20, 0)
    children = Seq(
      new VBox(6, new Label("Adres E-mail")        { style = labelStyle }, emailField),
      new VBox(6, new Label("Nazwa użytkownika")   { style = labelStyle }, usernameField),
      new VBox(6, new Label("Hasło")               { style = labelStyle }, passwordField),
      errorLabel,
      registerBtn
    )
  }

  private val switchBtn = new Button("Masz już konto? Zaloguj się") {
    padding = Insets(8, 20, 8, 20)
    style = secondaryBtnStyle
    onAction       = _ => onSwitchToLogin()
    onMouseEntered = _ => style = secondaryBtnStyle + "-fx-background-color: rgba(37, 99, 235, 0.25);"
    onMouseExited  = _ => style = secondaryBtnStyle
  }

  private val bottomSection = new VBox {
    alignment = Pos.Center
    spacing = 15
    padding = Insets(20, 0, 0, 0)
    children = Seq(
      switchBtn
    )
  }

  def showError(message: String): Unit = {
    errorLabel.text = message
    errorLabel.visible = true
  }

  val view: VBox = new VBox {
    padding = Insets(25, 30, 25, 30)
    prefWidth = 420
    maxWidth = 420
    maxHeight = Region.USE_PREF_SIZE
    style = s"""
      -fx-background-color: $modalBgColor;
      -fx-border-color: $borderColor;
      -fx-border-width: 1;
      -fx-background-radius: 12;
      -fx-border-radius: 12;
      -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 25, 0, 0, 15);
    """
    children = Seq(header, separator(), formBox, separator(), bottomSection)
  }
}