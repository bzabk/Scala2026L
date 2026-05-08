package SudokuGame.auth.domain

object PasswordPolicy extends InputPolicy {

  override protected val rules: List[Rule] = List(
    p => Option.when(p.length < 8)("Password must be at least 8 characters"),
    p => Option.when(!p.exists(_.isUpper))("Password must contain at least one uppercase letter"),
    p => Option.when(!p.exists(_.isLower))("Password must contain at least one lowercase letter"),
    p => Option.when(!p.exists(_.isDigit))("Password must contain at least one digit"),
    p => Option.when(!p.exists(ch => "!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(ch)))("Password must contain at least one special character")
  )

}