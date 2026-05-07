package SudokuGame.auth.domain

trait InputPolicy {
  protected type Rule = String => Option[String]
  protected val rules: List[Rule]
  def validate(input: String): Option[String] = rules.to(LazyList).flatMap(_(input)).headOption
}
