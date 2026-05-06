package SudokuGame.auth.domain

case class User(
   username: String,
   passwordHash: String,
   email: String,
   createdAt: Long
 )
