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
        data.get(key) match {
          case None =>
            Left(Seq(FormError(key, "フォームのname属性を正しく入力してください", Nil)))
          case Some(s) =>
            Try(s.toShort) match {
              case Failure(_) =>
                Left(Seq(FormError(key, "フォームのvalue属性を正しく入力してください", Nil)))
              case Success(code) =>
                CategoryColor.find(_.code == code) match {
                  case None =>
                    Left(Seq(FormError(key, "登録されている色を選択してください", Nil)))
                  case Some(categoryColor) =>
                    Right(categoryColor)
                }
            }
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
      // transform ver
//      "categoryColor" -> nonEmptyText.transform[CategoryColor](
//        str =>
//          Try(CategoryColor.find(_.code == str.toShort)) match {
//            case Failure(_) =>
//              CategoryColor.CategoryColor.COLOR_OPTION1
//            case Success(categoryColor) =>
//              categoryColor.getOrElse(CategoryColor.COLOR_OPTION1)
//        },
//        categoryColor => categoryColor.code.toString
//      )
    )(CategoryForm.apply)(CategoryForm.unapply)
  )

}
