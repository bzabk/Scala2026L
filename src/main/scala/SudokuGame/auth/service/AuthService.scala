package SudokuGame.auth.service

import SudokuGame.auth.domain.{LoginFailure, LoginResult, LoginSuccess, RegisterFailure, RegisterResult, RegisterSuccess}
import SudokuGame.auth.repository.{HttpUserRepository, UserRepository}
import org.mindrot.jbcrypt.BCrypt

import java.security.MessageDigest
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

  def login(email: String, password: String): Future[LoginResult] = {
    userRepository.login(email, password)
      .map {
        case true  => LoginSuccess
        case false => LoginFailure("Invalid email or password")
      }
      .recover { case _ => LoginFailure("Connection error. Please try again later") }
  }
}