package SudokuGame.auth.domain

object UserNamePolicy extends InputPolicy {

  override protected val rules: List[Rule] = List(
    u => Option.when(u.length < 3)("Nazwa użytkownika musi mieć co najmniej 3 znaki"),
    u => Option.when(!u.forall(_.isLetterOrDigit))("Nazwa użytkownika nie może zawierać spacji ani znaków specjalnych")
  )

}