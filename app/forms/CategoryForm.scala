package forms

import lib.model.Category.CategoryColor
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formatter

import scala.util.{Try, Success, Failure}

case class CategoryForm(
  name: String,
  slug: String,
  categoryColor: CategoryColor
)

object CategoryForm {

  implicit val categoryColorFormat: Formatter[CategoryColor] =
    new Formatter[CategoryColor] {
      override def bind(
        key: String,
        data: Map[String, String]
      ): Either[Seq[FormError], CategoryColor] = {
        for {
          str <- data.get(key).toRight("フォームのname属性を正しく入力してください")
          code <- Try(str.toShort).toEither.left.map(_ =>
            "フォームのvalue属性を正しく入力してください")
          color <- Try(CategoryColor(code)).toEither.left.map(_ =>
            "登録されているカテゴリーのみを登録してください")
        } yield color
      }.left.map(errMsg => Seq(FormError(key, errMsg, Nil)))

      override def unbind(
        key: String,
        value: CategoryColor
      ): Map[String, String] =
        Map(key -> value.toString)
    }

  val categoryForm: Form[CategoryForm] = Form(
    mapping(
      "name" -> nonEmptyText.verifying(
        "カテゴリ名は英数字・日本語を入力することができ、改行を含むことができません",
        name =>
          name
            .matches("[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+") && !name
            .contains("\n")
      ),
      "slug" -> nonEmptyText.verifying(
        "英数字以外を入力することはできません",
        slug => slug.matches("[a-zA-Z0-9]+") && !slug.contains("\n")
      ),
      "categoryColor" -> of[CategoryColor]
      // transform ver
      // "categoryColor" -> shortNumber.transform[CategoryColor](CategoryColor(_), _.code)
    )(CategoryForm.apply)(CategoryForm.unapply)
  )

}
