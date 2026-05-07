package SudokuGame.auth.repository

import scala.concurrent.Future

trait UserRepository {
  def register(email: String, username: String, passwordHash: String): Future[Boolean]
  def login(email: String, password: String): Future[Boolean]
}