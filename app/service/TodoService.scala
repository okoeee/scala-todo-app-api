package service

import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}
import model.ViewValueTodo

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object TodoService {

  def getSeqViewValueTodo: Future[Seq[ViewValueTodo]] = {
    (TodoRepository.getAll zip CategoryRepository.getAll).map {
      case (todos, categories) =>
        todos.flatMap { embeddedTodo =>
          val todo = embeddedTodo.v
          val optCategory = categories.find(_.id == todo.categoryId).map(_.v)
          optCategory.map { category =>
            ViewValueTodo(
              id = embeddedTodo.id,
              title = todo.title,
              body = todo.body,
              status = todo.state,
              categoryName = category.name,
              categoryColor = category.categoryColor
            )
          }
        }
    }
  }

}
