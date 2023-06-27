package requests

import lib.model.Category.CategoryColor

case class TodoForm(
  title: String,
  body: String,
  categoryId: Long
)
