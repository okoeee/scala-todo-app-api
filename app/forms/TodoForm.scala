package forms

import ixias.model.tag

import lib.model.{Category, Todo}
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formatter

import scala.util.{Try, Success, Failure}

case class TodoForm(
  title: String,
  body: String,
  categoryId: Category.Id,
  state: Todo.Status
)

object TodoForm {
  implicit val categoryIdFormat: Formatter[Category.Id] =
    new Formatter[Category.Id] {
      override def bind(
        key: String,
        data: Map[String, String]
      ): Either[Seq[FormError], Category.Id] = {
        data.get(key) match {
          case None => Left(Seq(FormError(key, "フォームのname属性を正しく入力してください", Nil)))
          case Some(s) =>
            Try(s.toLong) match {
              case Failure(_) =>
                Left(Seq(FormError(key, "フォームのvalue属性を正しく入力してください", Nil)))
              case Success(id) =>
                Right(tag[Category.Id](id).asInstanceOf[Category.Id])
            }
        }
      }

      override def unbind(
        key: String,
        value: Category.Id
      ): Map[String, String] =
        Map(key -> value.toString)
    }

  implicit val todoStatusFormat = new Formatter[Todo.Status] {
    override def bind(
      key: String,
      data: Map[String, String]): Either[Seq[FormError], Todo.Status] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "フォームのname属性を正しく入力してください", Nil)))
        case Some(str) =>
          Try(str.toShort) match {
            case Failure(_) =>
              Left(Seq(FormError(key, "フォームのvalue属性を正しく入力してください", Nil)))
            case Success(code) =>
              Todo.Status.find(_.code == code) match {
                case None =>
                  Left(Seq(FormError(key, "登録されているステータスを選択してください", Nil)))
                case Some(todoStatus) => Right(todoStatus)
              }
          }
      }
    }

    override def unbind(key: String, value: Todo.Status): Map[String, String] =
      Map(key -> value.toString)
  }

  val todoForm: Form[TodoForm] = Form(
    mapping(
      "title" -> nonEmptyText.verifying(
        "タイトルは英数字・日本語を入力することができ、改行を含むことができません",
        title =>
          title
            .matches("[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+") && !title
            .contains("\n")
      ),
      "body" -> nonEmptyText.verifying(
        "本文は英数字・日本語のみを入力することができます",
        body =>
          body.matches(
            "[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}\\r\\n]+")
      ),
      "categoryId" -> of[Category.Id],
      "state" -> of[Todo.Status]
    )(TodoForm.apply)(TodoForm.unapply)
  )
}
