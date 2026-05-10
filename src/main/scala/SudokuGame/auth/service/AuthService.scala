package SudokuGame.auth.service

import SudokuGame.auth.domain.{LoggedUser, LoginFailure, LoginResult, LoginSuccess, RegisterFailure, RegisterResult, RegisterSuccess}
import SudokuGame.auth.repository.HttpUserRepository
import scala.concurrent.{ExecutionContext, Future}

class AuthService(userRepository: HttpUserRepository)(implicit ec: ExecutionContext) {
  

  def register(email: String, username: String, password: String): Future[RegisterResult] = {
    userRepository.register(email, username, password)
      .map {
        case true  => RegisterSuccess
        case false => RegisterFailure("Registration failed: user already exists")
      }
      .recover { case _ => RegisterFailure("Connection error. Please try again later") }
  }

  def login(username: String, password: String): Future[LoginResult] = {
    userRepository.login(username, password)
      .map {
        case true  => LoginSuccess(LoggedUser(username))
        case false => LoginFailure("Invalid email or password")
      }
      .recover { case _ => LoginFailure("Connection error. Please try again later") }
  }
}