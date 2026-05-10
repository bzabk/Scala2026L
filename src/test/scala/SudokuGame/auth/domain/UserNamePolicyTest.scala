package SudokuGame.auth.domain

class UserNamePolicyTest extends munit.FunSuite {

  test("username with 3 or more alphanumeric characters returns None") {
    assertEquals(UserNamePolicy.validate("abc"), None)
    assertEquals(UserNamePolicy.validate("User123"), None)
    assertEquals(UserNamePolicy.validate("johndoe"), None)
  }

  test("empty string returns a length error") {
    assert(UserNamePolicy.validate("").isDefined)
  }

  test("username shorter than 3 characters returns a length error") {
    assert(UserNamePolicy.validate("ab").isDefined)
    assert(UserNamePolicy.validate("a").isDefined)
  }

  test("username with a space returns error") {
    assert(UserNamePolicy.validate("user name").isDefined)
  }

  test("username with special characters returns error") {
    assert(UserNamePolicy.validate("user!").isDefined)
    assert(UserNamePolicy.validate("us@er").isDefined)
    assert(UserNamePolicy.validate("use-r").isDefined)
  }

  test("length error takes priority over special-character error") {
    assertEquals(
      UserNamePolicy.validate("a!"),
      Some("Nazwa użytkownika musi mieć co najmniej 3 znaki")
    )
  }

  test("special-character error produces the correct message") {
    assertEquals(
      UserNamePolicy.validate("user name"),
      Some("Nazwa użytkownika nie może zawierać spacji ani znaków specjalnych")
    )
  }
}