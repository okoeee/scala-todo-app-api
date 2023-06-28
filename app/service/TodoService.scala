package service

import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}
import model.ViewValueTodo

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object TodoService {

  def getSeqViewValueTodo: Future[Seq[ViewValueTodo]] = {
    (TodoRepository.getAll zip CategoryRepository.getAll).map {
      case (todos, categories) =>
        todos.map { embeddedTodo =>
          val todo = embeddedTodo.v
          val optCategory = categories.find(_.id == todo.categoryId).map(_.v)
          ViewValueTodo(
            id = embeddedTodo.id,
            title = todo.title,
            body = todo.body,
            status = todo.state,
            categoryName = optCategory.map(_.name).getOrElse("カテゴリなし"),
            categoryColor = optCategory
              .map(_.categoryColor)
              .getOrElse(CategoryColor.COLOR_OPTION_NONE)
          )
        }
    }
  }

}
