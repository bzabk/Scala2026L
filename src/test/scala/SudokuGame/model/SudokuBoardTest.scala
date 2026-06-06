package SudokuGame.model

import scala.collection.immutable.SortedSet

class SudokuBoardTest extends munit.FunSuite {
  test("loadSnapshot restores values and notes while preserving given cells") {
    val initialBoard = Array(
      Array(5, 0, 0, 0, 0, 0, 0, 0, 0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0),
      Array.fill(9)(0)
    )
    val values = Seq.tabulate(9, 9) { (row, col) =>
      if row == 0 && col == 1 then 4 else initialBoard(row)(col)
    }
    val notes = Seq.tabulate(9, 9) { (row, col) =>
      if row == 1 && col == 1 then Seq(3, 8) else Seq.empty[Int]
    }
    val board = new SudokuBoard(initialBoard)

    board.loadSnapshot(values, notes)

    assert(board.isCellGiven(0, 0))
    assert(!board.isCellGiven(0, 1))
    assertEquals(board.getCellValue(0, 1), 4)
    assertEquals(board.getCellNotes(1, 1), SortedSet(3, 8))
    assertEquals(board.values(0).take(2), Seq(5, 4))
    assertEquals(board.notes(1)(1), Seq(3, 8))
  }

  test("notes mode toggles notes and clears an existing value") {
    val board = new SudokuBoard()

    board.updateCell(0, 0, 4, isNotesMode = false)
    board.updateCell(0, 0, 7, isNotesMode = true)

    assertEquals(board.getCellValue(0, 0), 0)
    assertEquals(board.getCellNotes(0, 0), SortedSet(4, 7))

    board.updateCell(0, 0, 7, isNotesMode = true)

    assertEquals(board.getCellNotes(0, 0), SortedSet(4))
  }

  test("conflicts are marked for duplicates and cleared after fixing the row") {
    val board = new SudokuBoard()

    board.updateCell(0, 0, 5, isNotesMode = false)
    board.updateCell(0, 1, 5, isNotesMode = false)

    assert(board.conflicts(0)(0).contains(5))
    assert(board.conflicts(0)(1).contains(5))

    board.updateCell(0, 1, 0, isNotesMode = false)

    assert(board.conflicts(0)(0).isEmpty)
    assert(board.conflicts(0)(1).isEmpty)
  }

  test("loadSnapshot tolerates partial data and filters invalid note values") {
    val board = new SudokuBoard()

    board.loadSnapshot(
      values = Seq.empty,
      notes = Seq(Seq(Seq(0, 1, 3, 10)))
    )

    assertEquals(board.getCellValue(8, 8), 0)
    assertEquals(board.getCellNotes(0, 0), SortedSet(1, 3))
  }

  test("column and box duplicates are reported as conflicts") {
    val board = new SudokuBoard()

    board.updateCell(0, 0, 6, isNotesMode = false)
    board.updateCell(1, 0, 6, isNotesMode = false)
    board.updateCell(1, 1, 6, isNotesMode = false)

    assert(board.conflicts(0)(0).contains(6))
    assert(board.conflicts(1)(0).contains(6))
    assert(board.conflicts(1)(1).contains(6))
  }

  test("isSolved requires every cell to be filled and valid") {
    val solved = Array(
      Array(5, 3, 4, 6, 7, 8, 9, 1, 2),
      Array(6, 7, 2, 1, 9, 5, 3, 4, 8),
      Array(1, 9, 8, 3, 4, 2, 5, 6, 7),
      Array(8, 5, 9, 7, 6, 1, 4, 2, 3),
      Array(4, 2, 6, 8, 5, 3, 7, 9, 1),
      Array(7, 1, 3, 9, 2, 4, 8, 5, 6),
      Array(9, 6, 1, 5, 3, 7, 2, 8, 4),
      Array(2, 8, 7, 4, 1, 9, 6, 3, 5),
      Array(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )
    val board = new SudokuBoard(solved)

    assert(board.isSolved)

    board.updateCell(0, 0, 6, isNotesMode = false)

    assert(!board.isSolved)
    assert(!board.isValid)
  }
}
