package SudokuGame.model

import scala.collection.immutable.SortedSet

class GameStateTest extends munit.FunSuite {
  private val move = BoardMove(
    row = 0,
    col = 1,
    previousValue = 0,
    previousNotes = SortedSet.empty,
    newValue = 4,
    newNotes = SortedSet.empty
  )

  test("formatTime renders elapsed seconds as mm:ss") {
    val state = GameState(new SudokuBoard(), elapsedSeconds = 65)

    assertEquals(state.formatTime(), "01:05")
  }

  test("recordMove pushes undo history and clears redo history") {
    val previousRedo = move.copy(col = 2, newValue = 7)
    val state = GameState(new SudokuBoard(), redoHistory = List(previousRedo))

    val updated = state.recordMove(move)

    assertEquals(updated.moveHistory, List(move))
    assertEquals(updated.redoHistory, Nil)
  }

  test("popUndoMove moves the latest move into redo history") {
    val state = GameState(new SudokuBoard(), moveHistory = List(move))

    val (updated, popped) = state.popUndoMove()

    assertEquals(popped, Some(move))
    assertEquals(updated.moveHistory, Nil)
    assertEquals(updated.redoHistory, List(move))
  }

  test("popRedoMove moves the latest redo move back into undo history") {
    val state = GameState(new SudokuBoard(), redoHistory = List(move))

    val (updated, popped) = state.popRedoMove()

    assertEquals(popped, Some(move))
    assertEquals(updated.moveHistory, List(move))
    assertEquals(updated.redoHistory, Nil)
  }

  test("isLost becomes true once error count reaches the maximum") {
    val state = GameState(new SudokuBoard(), errorCount = 3, maxErrors = 3)

    assert(state.isLost)
  }
}
