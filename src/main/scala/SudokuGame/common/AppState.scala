package SudokuGame.common

import SudokuGame.auth.domain.LoggedUser
import scalafx.beans.property.ObjectProperty

class AppState {
  val currentUser: ObjectProperty[Option[LoggedUser]] = ObjectProperty(None)
  val recentGames: ObjectProperty[List[GameState]] = ObjectProperty(Nil)
}