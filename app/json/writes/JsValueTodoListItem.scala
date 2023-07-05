package json.writes

import model.ViewValueTodo
import play.api.libs.json.{Json, Writes}

case class JsValueTodoListItem(
  id: Long,
  title: String,
  body: String,
  status: Short,
  categoryId: Long,
  categoryName: String,
  categoryColor: Short
)

object JsValueTodoListItem {
  implicit val writes: Writes[JsValueTodoListItem] =
    Json.writes[JsValueTodoListItem]

  def apply(viewValueTodo: ViewValueTodo): JsValueTodoListItem = {
    JsValueTodoListItem(
      id = viewValueTodo.id,
      title = viewValueTodo.title,
      body = viewValueTodo.body,
      status = viewValueTodo.status.code,
      categoryId = viewValueTodo.categoryId,
      categoryName = viewValueTodo.categoryName,
      categoryColor = viewValueTodo.categoryColor.code
    )
  }
}
