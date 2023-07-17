package forms

case class LoginForm(
  email: String,
  password: String
)

object LoginForm {
  import play.api.data._
  import play.api.data.Forms._

  val loginForm: Form[LoginForm] = Form(
    mapping(
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
    )(LoginForm.apply)(LoginForm.unapply)
  )
}
