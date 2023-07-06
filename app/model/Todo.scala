package model

import lib.model.{Category, Todo}
import lib.model.Todo.Status
import lib.model.Category.CategoryColor

case class ViewValueTodo(
  id: Todo.Id,
  title: String,
  body: String,
  status: Status,
  categoryId: Category.Id,
  categoryName: String,
  categoryColor: CategoryColor
)
