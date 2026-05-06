package SudokuGame.auth.service

import SudokuGame.auth.domain.User
import SudokuGame.auth.repository.HttpUserRepository
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.{ExecutionContext, Future}

class AuthService(userRepository: HttpUserRepository)(implicit ec: ExecutionContext) {


  def register(email: String, password: String): Future[Either[Boolean, String]] = {
    val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
    userRepository.register(email,passwordHash).map {
      case true => Left(true)
      case _ => Right("Registration failed: User already exists or database error")
    }
  }

  def login(email: String, password: String): Future[Either[Boolean, String]] = {
    userRepository.login(email, password).map { response =>
      response match {
        case true => Left(true)
        case false => Right("Login failed: Invalid email or password")
      }
      
    }
  }
}
  

