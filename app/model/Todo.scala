package model

import lib.model.Todo.Status
import lib.model.Category.CategoryColor

case class ViewValueTodo (
  id: Long,
  title:    String,
  body:     String,
  status:   Status,
  categoryName: String,
  categoryColor: CategoryColor
)
