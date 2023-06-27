package requests

case class TodoForm(
  title: String,
  body: String,
  categoryId: Long,
  state: Short
)
