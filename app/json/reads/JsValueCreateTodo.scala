package json.reads

import ixias.util.json.JsonEnvReads
import lib.model.Category
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class JsValueCreateTodo(
  title: String,
  body: String,
  categoryId: Category.Id
)

object JsValueCreateTodo extends JsonEnvReads {
  implicit val reads: Reads[JsValueCreateTodo] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "categoryId").read[Category.Id](idAsNumberReads[Category.Id])
  )(JsValueCreateTodo.apply _)
}
