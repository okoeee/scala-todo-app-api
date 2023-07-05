package json.reads

import lib.model.{Category, Todo}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class JsValueUpdateTodo(
  title: String,
  body: String,
  categoryId: Category.Id,
  status: Todo.Status
)

object JsValueUpdateTodo {
  implicit val reads: Reads[JsValueUpdateTodo] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "categoryId")
        .read[Long]
        .map(categoryId => Category.Id(categoryId)) and
      (JsPath \ "status").read[Short].map(status => Todo.Status(status))
  )(JsValueUpdateTodo.apply _)
}
