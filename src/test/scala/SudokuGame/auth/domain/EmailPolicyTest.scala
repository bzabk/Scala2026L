package SudokuGame.auth.domain

class EmailPolicyTest extends munit.FunSuite {

  test("empty string returns an error") {
    assert(EmailPolicy.validate("").isDefined)
  }

  test("whitespace-only string returns an error") {
    assert(EmailPolicy.validate("   ").isDefined)
  }

  test("valid email returns None") {
    assertEquals(EmailPolicy.validate("user@example.com"), None)
    assertEquals(EmailPolicy.validate("user.name+tag@sub.domain.org"), None)
    assertEquals(EmailPolicy.validate("a@b.pl"), None)
  }

  test("missing @ returns a format error") {
    assert(EmailPolicy.validate("userexample.com").isDefined)
  }

  test("missing domain after @ returns a format error") {
    assert(EmailPolicy.validate("user@").isDefined)
  }

  test("missing domain extension returns a format error") {
    assert(EmailPolicy.validate("user@domain").isDefined)
  }

  test("space inside address returns a format error") {
    assert(EmailPolicy.validate("user @example.com").isDefined)
  }

  test("single-character domain extension returns a format error") {
    assert(EmailPolicy.validate("user@domain.c").isDefined)
  }

  test("empty input error takes priority over format error") {
    assertEquals(EmailPolicy.validate(""), Some("Email nie może być pusty"))
  }

  test("invalid format produces the correct error message") {
    assertEquals(EmailPolicy.validate("notanemail"), Some("Nieprawidłowy format email"))
  }
}