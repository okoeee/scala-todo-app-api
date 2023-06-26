package lib.model

import ixias.model._
import ixias.util.EnumStatus
import lib.model.Todo.Status

import java.time.LocalDateTime

case class Todo (
  id: Option[Todo.Id],
  categoryId: Long,
  title: String,
  body: String,
  state: Status,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Todo.Id]

object Todo {
  val Id = the[Identity[Id]]
  type Id = Long @@ Todo

  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {
    case object IS_STARTED extends Status(code = 0, name = "着手中")
    case object IS_PROGRESSIVE extends Status(code = 1, name = "進行中")
    case object IS_COMPLETED extends Status(code = 2, name = "完了")
  }

  def apply(categoryId: Long, title: String, body: String, state: Status): Todo#WithNoId =
    new Todo(
      id = None,
      categoryId = categoryId,
      title = title,
      body = body,
      state = state
    ).toWithNoId
}