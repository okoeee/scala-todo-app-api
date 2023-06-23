package lib.model

import ixias.model._

import java.time.LocalDateTime

case class Todo (
  id: Option[Todo.Id],
  categoryId: Long,
  title: String,
  body: String,
  state: Int,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Todo.Id]

object Todo {
  val Id = the[Identity[Id]]
  type Id = Long @@ Todo

  def apply(categoryId: Long, title: String, body: String, state: Int): Todo#WithNoId =
    new Todo(
      id = None,
      categoryId = categoryId,
      title = title,
      body = body,
      state = state
    ).toWithNoId
}