package SudokuGame.auth.domain

case class LoggedUser(username: String)

sealed trait LoginResult
case class  LoginSuccess(user: LoggedUser) extends LoginResult
case class  LoginFailure(reason: String)   extends LoginResult

sealed trait RegisterResult
case object RegisterSuccess                  extends RegisterResult
case class  RegisterFailure(reason: String)  extends RegisterResult
