package service

import lib.model.Category
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

  /**
    * Categoryを削除した際、どのカテゴリーにも紐づいていないことを表すためにcategoryIdを0に変更する
    */
  def updateTodosOfNoneCategory(categoryId: Category.Id): Future[Int] = {
    TodoRepository.getAllFilteredByCategoryId(categoryId).flatMap { seqEmbeddedTodo =>
      val seqUpdatedTodo = seqEmbeddedTodo.map {
        _.map(_.copy(categoryId = Category.Id(0)))
      }
      TodoRepository.bulkUpdate(seqUpdatedTodo)
    }
  }

}
