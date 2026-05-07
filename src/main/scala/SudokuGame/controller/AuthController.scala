package SudokuGame.controller

import SudokuGame.auth.domain.{LoginFailure, LoginSuccess, RegisterFailure, RegisterSuccess}
import SudokuGame.auth.service.AuthService
import SudokuGame.auth.ui.{LoginView, RegisterView}
import SudokuGame.ui.layout.MainApplicationLayout
import scalafx.animation.PauseTransition
import scalafx.application.Platform
import scalafx.util.Duration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class AuthController(
  mainLayout:  MainApplicationLayout,
  authService: AuthService
) {

  private var loginView:    LoginView    = _
  private var registerView: RegisterView = _

  def showLoginView(): Unit = {
    loginView = new LoginView(
      onClose            = () => mainLayout.hideOverlay(),
      onLogin            = (email, password) => performLogin(email, password),
      onSwitchToRegister = () => showRegisterView()
    )
    mainLayout.showOverlay(loginView.view)
  }

  def showRegisterView(): Unit = {
    registerView = new RegisterView(
      onClose         = () => mainLayout.hideOverlay(),
      onRegister      = (email, username, password) => performRegister(email, username, password),
      onSwitchToLogin = () => showLoginView()
    )
    mainLayout.showOverlay(registerView.view)
  }

  private def closeWithDelay(message: String): Unit = {
    val pause = new PauseTransition(Duration(700))
    loginView.showSuccess(message)
    pause.onFinished = _ => mainLayout.hideOverlay()
    pause.play()
  }

  private def performLogin(email: String, password: String): Unit = {
    authService.login(email, password).onComplete {
      case Success(LoginSuccess) =>
        println(email)
        println(password)
        Platform.runLater(closeWithDelay("Zalogowano pomyślnie!"))
      case Success(LoginFailure(reason)) =>
        Platform.runLater {
          if (loginView != null) loginView.showError(reason)
        }
      case Failure(_) =>
        Platform.runLater {
          if (loginView != null) loginView.showError("Niespodziewany błąd. Spróbuj ponownie później")
        }
    }
  }

  private def performRegister(email: String, username: String, password: String): Unit = {
    authService.register(email, username, password).onComplete {
      case Success(RegisterSuccess) =>
        Platform.runLater(closeWithDelay("Zarejestrowano pomyślnie! Możesz teraz się zalogować"))
      case Success(RegisterFailure(reason)) =>
        Platform.runLater {
          if (registerView != null) registerView.showError(reason)
        }
      case Failure(_) =>
        Platform.runLater {
          if (registerView != null) registerView.showError("Niespodziewany błąd. Spróbuj ponownie później")
        }
    }
  }
}