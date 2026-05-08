package SudokuGame.auth.domain

object UserNamePolicy extends InputPolicy {

  override protected val rules: List[Rule] = List(
    u => Option.when(u.length < 3)("Username must be at least 3 characters"),
    u => Option.when(!u.forall(_.isLetterOrDigit))("Username must not contain spaces or special characters")
  )

}