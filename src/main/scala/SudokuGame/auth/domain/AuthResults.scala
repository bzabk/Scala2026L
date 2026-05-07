package SudokuGame.auth.domain

sealed trait LoginResult
case object LoginSuccess                  extends LoginResult
case class  LoginFailure(reason: String)  extends LoginResult

sealed trait RegisterResult
case object RegisterSuccess                  extends RegisterResult
case class  RegisterFailure(reason: String)  extends RegisterResult
