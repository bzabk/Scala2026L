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
        case false => RegisterFailure("Rejestracja nieudana: użytkownik już istnieje")
      }
      .recover { case _ => RegisterFailure("Błąd połączenia z serwerem") }
  }

  def login(email: String, password: String): Future[LoginResult] = {
    userRepository.login(email, password)
      .map {
        case true  => LoginSuccess
        case false => LoginFailure("Nieprawidłowy email lub hasło")
      }
      .recover { case _ => LoginFailure("Błąd połączenia z serwerem") }
  }
}