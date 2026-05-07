package SudokuGame.auth.domain

object EmailPolicy extends InputPolicy {

  private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

  override protected val rules: List[Rule] = List(
    e => Option.when(e.trim.isEmpty)("Email nie może być pusty"),
    e => Option.when(emailRegex.matches(e).unary_!)("Nieprawidłowy format email")
  )

}