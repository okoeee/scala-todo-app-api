package requests

import lib.model.{Category, Todo}

case class TodoForm(
  title: String,
  body: String,
  categoryId: Category.Id,
  state: Todo.Status
)
