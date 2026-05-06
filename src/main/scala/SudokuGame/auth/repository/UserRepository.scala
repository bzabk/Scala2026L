package SudokuGame.auth.repository

import SudokuGame.auth.domain.User

import scala.concurrent.Future

trait UserRepository {

  def register(email: String, password: String): Future[Boolean]
  
  def login(email: String, password: String): Future[Boolean]

}
