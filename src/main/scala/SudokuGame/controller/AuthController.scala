package SudokuGame.controller

import SudokuGame.auth.domain.{EmailPolicy, LoginFailure, LoginSuccess, PasswordPolicy, RegisterFailure, RegisterSuccess, UserNamePolicy}
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

  private def performLogin(username: String, password: String): Unit = {

    authService.login(username, password).onComplete {
      case Success(LoginSuccess) =>
        Platform.runLater(closeWithDelay("Logged in successfully!"))
      case Success(LoginFailure(reason)) =>
        Platform.runLater {
          if (loginView != null) loginView.showError(reason)
        }
      case Failure(_) =>
        Platform.runLater {
          if (loginView != null) loginView.showError("Unexpected error. Please try again later")
        }
    }
  }

  private def performRegister(email: String, username: String, password: String): Unit = {
    val validationError =
      EmailPolicy.validate(email) orElse
      UserNamePolicy.validate(username) orElse
      PasswordPolicy.validate(password)

    validationError match {
      case Some(error) =>
        if (registerView != null) registerView.showError(error)
        return
      case None =>
    }

    authService.register(email, username, password).onComplete {
      case Success(RegisterSuccess) =>
        Platform.runLater(closeWithDelay("Account created! You can now sign in."))
      case Success(RegisterFailure(reason)) =>
        Platform.runLater {
          if (registerView != null) registerView.showError(reason)
        }
      case Failure(_) =>
        Platform.runLater {
          if (registerView != null) registerView.showError("Unexpected error. Please try again later")
        }
    }
  }
}