package forms

case class RegistrationForm(
  name: String,
  email: String,
  password: String
)

object RegistrationForm {
  import play.api.data._
  import play.api.data.Forms._

  val registrationForm: Form[RegistrationForm] = Form(
    mapping(
      "name" -> nonEmptyText.verifying(
        "名前は英数字・日本語を入力することができ、改行を含むことができません",
        name =>
          name
            .matches("[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+") && !name
            .contains("\n")
      ),
      "email" -> nonEmptyText.verifying(
        "メールアドレスは改行を含むことができません",
        email => !email.contains("\n")
      ),
      "password" -> nonEmptyText.verifying(
        "パスワードは英数字・日本語を入力することができ、改行を含むことができません",
        password =>
          password
            .matches("[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+") && !password
            .contains("\n")
      )
    )(RegistrationForm.apply)(RegistrationForm.unapply)
  )
}
