package model

import lib.model.Category
import lib.model.Todo.Status
import lib.model.Category.CategoryColor

case class ViewValueTodo(
  id: Long,
  title: String,
  body: String,
  status: Status,
  categoryId: Category.Id,
  categoryName: String,
  categoryColor: CategoryColor
)
