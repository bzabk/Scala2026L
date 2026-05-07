package SudokuGame.auth.domain

object PasswordPolicy extends InputPolicy {

  override protected val rules: List[Rule] = List(
    p => Option.when(p.length < 8)("Hasło musi mieć co najmniej 8 znaków"),
    p => Option.when(!p.exists(_.isUpper))("Hasło musi zawierać co najmniej jedną wielką literę"),
    p => Option.when(!p.exists(_.isLower))("Hasło musi zawierać co najmniej jedną małą literę"),
    p => Option.when(!p.exists(_.isDigit))("Hasło musi zawierać co najmniej jedną cyfrę"),
    p => Option.when(!p.exists(ch => "!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(ch)))("Hasło musi zawierać co najmniej jeden znak specjalny")
  )

}