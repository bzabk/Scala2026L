package SudokuGame.auth.domain

class PasswordPolicyTest extends munit.FunSuite {

  test("valid password returns None") {
    assertEquals(PasswordPolicy.validate("Secure1!"), None)
    assertEquals(PasswordPolicy.validate("MyP@ssw0rd"), None)
  }

  test("password shorter than 8 characters returns a length error") {
    assert(PasswordPolicy.validate("Ab1!").isDefined)
  }

  test("password without an uppercase letter returns an error") {
    assert(PasswordPolicy.validate("secure1!").isDefined)
  }

  test("password without a lowercase letter returns an error") {
    assert(PasswordPolicy.validate("SECURE1!").isDefined)
  }

  test("password without a digit returns an error") {
    assert(PasswordPolicy.validate("SecurePass!").isDefined)
  }

  test("password without a special character returns an error") {
    assert(PasswordPolicy.validate("Secure123").isDefined)
  }

  test("length error takes priority over all other errors") {
    assertEquals(
      PasswordPolicy.validate("Ab1!"),
      Some("Hasło musi mieć co najmniej 8 znaków")
    )
  }

  test("missing uppercase produces the correct error message") {
    assertEquals(
      PasswordPolicy.validate("secure1!"),
      Some("Hasło musi zawierać co najmniej jedną wielką literę")
    )
  }

  test("missing lowercase produces the correct error message") {
    assertEquals(
      PasswordPolicy.validate("SECURE1!"),
      Some("Hasło musi zawierać co najmniej jedną małą literę")
    )
  }

  test("missing digit produces the correct error message") {
    assertEquals(
      PasswordPolicy.validate("SecurePass!"),
      Some("Hasło musi zawierać co najmniej jedną cyfrę")
    )
  }

  test("missing special character produces the correct error message") {
    assertEquals(
      PasswordPolicy.validate("Secure123"),
      Some("Hasło musi zawierać co najmniej jeden znak specjalny")
    )
  }


}