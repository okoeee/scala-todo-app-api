package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueUpdateTodo(
  title: String,
  body: String,
  categoryId: Long,
  status: Short
)

object JsValueUpdateTodo {
  implicit val reads: Reads[JsValueUpdateTodo] = Json.reads[JsValueUpdateTodo]
}
