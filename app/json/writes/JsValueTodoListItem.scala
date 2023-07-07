package json.writes

import ixias.util.json.JsonEnvWrites
import lib.model.{Category, Todo}
import model.ViewValueTodo
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Writes}

case class JsValueTodoListItem(
  id: Todo.Id,
  title: String,
  body: String,
  status: Todo.Status,
  categoryId: Category.Id,
  categoryName: String,
  categoryColor: Category.CategoryColor
)

object JsValueTodoListItem extends JsonEnvWrites {

  implicit val writes: Writes[JsValueTodoListItem] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "title").write[String] and
      (JsPath \ "body").write[String] and
      (JsPath \ "status").write(EnumStatusWrites) and
      (JsPath \ "categoryId").write[Long] and
      (JsPath \ "categoryName").write[String] and
      (JsPath \ "categoryColor")
        .write(EnumStatusWrites)
  )(unlift(JsValueTodoListItem.unapply))

  def apply(viewValueTodo: ViewValueTodo): JsValueTodoListItem = {
    JsValueTodoListItem(
      id = viewValueTodo.id,
      title = viewValueTodo.title,
      body = viewValueTodo.body,
      status = viewValueTodo.status,
      categoryId = viewValueTodo.categoryId,
      categoryName = viewValueTodo.categoryName,
      categoryColor = viewValueTodo.categoryColor
    )
  }
}
