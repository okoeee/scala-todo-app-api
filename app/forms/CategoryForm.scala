package forms

import lib.model.Category.CategoryColor
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formatter

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
        data: Map[String, String]): Either[Seq[FormError], CategoryColor] = {
        data.get(key) match {
          case Some(s) =>
            try {
              Right(
                CategoryColor
                  .find(_.code == s.toShort)
                  .getOrElse(CategoryColor.COLOR_OPTION_NONE))
            } catch {
              case _: NumberFormatException =>
                Left(Seq(FormError(key, "error.number", Nil)))
            }
          case None => Left(Seq(FormError(key, "error.required", Nil)))
        }
      }

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
    )(CategoryForm.apply)(CategoryForm.unapply)
  )

}
