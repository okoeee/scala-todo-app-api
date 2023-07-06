package json.reads

import ixias.util.json.JsonEnvReads
import lib.model.{Category, Todo}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class JsValueUpdateTodo(
  title: String,
  body: String,
  categoryId: Category.Id,
  status: Todo.Status
)

object JsValueUpdateTodo extends JsonEnvReads {
  implicit val reads: Reads[JsValueUpdateTodo] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "categoryId")
        .read[Category.Id](idAsNumberReads[Category.Id]) and
      (JsPath \ "status").read[Todo.Status](enumReads[Todo.Status](Todo.Status))
  )(JsValueUpdateTodo.apply _)
}
