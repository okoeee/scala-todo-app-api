package service

import json.reads.JsValueUpdateTodo
import lib.model.{Category, Todo}
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
            categoryId = optCategory.flatMap(_.id).getOrElse(Category.Id(0)),
            categoryName = optCategory.map(_.name).getOrElse("カテゴリなし"),
            categoryColor = optCategory
              .map(_.categoryColor)
              .getOrElse(CategoryColor.COLOR_OPTION_NONE)
          )
        }
    }
  }

  def getViewValueTodo(id: Todo.Id): Future[Either[String, ViewValueTodo]] = {
    TodoRepository.get(id).flatMap {
      case None => Future.successful(Left("Todoが見つかりませんでした"))
      case Some(embeddedTodo) =>
        CategoryRepository.get(embeddedTodo.v.categoryId).map {
          optEmbeddedCategory =>
            Right(
              ViewValueTodo(
                id = embeddedTodo.id,
                title = embeddedTodo.v.title,
                body = embeddedTodo.v.body,
                status = embeddedTodo.v.state,
                categoryId =
                  optEmbeddedCategory.map(_.id).getOrElse(Category.Id(0)),
                categoryName =
                  optEmbeddedCategory.map(_.v.name).getOrElse("カテゴリなし"),
                categoryColor = optEmbeddedCategory
                  .map(_.v.categoryColor)
                  .getOrElse(CategoryColor.COLOR_OPTION_NONE)
              )
            )
        }
    }
  }

  def updateTodo(
    id: Long,
    todoFormData: JsValueUpdateTodo
  ): Future[Either[String, String]] = {
    TodoRepository.get(Todo.Id(id)).flatMap {
      case None =>
        Future.successful(Left("更新対象のTodoが見つかりませんでした"))
      case Some(embeddedTodo) =>
        val updatedTodo = embeddedTodo.map(
          _.copy(
            title = todoFormData.title,
            body = todoFormData.body,
            categoryId = Category.Id(todoFormData.categoryId),
            state = Todo.Status(todoFormData.status)
          ))

        TodoRepository.update(updatedTodo).map { _ =>
          Right("Todoを更新しました")
        } recover {
          case _: Exception =>
            Left("Todoの更新に失敗しました")
        }
    }
  }

  /**
    * Categoryを削除した際、どのカテゴリーにも紐づいていないことを表すためにcategoryIdを0に変更する
    */
  def updateTodosOfNoneCategory(categoryId: Category.Id): Future[Int] = {
    TodoRepository.getAllFilteredByCategoryId(categoryId).flatMap {
      seqEmbeddedTodo =>
        val seqUpdatedTodo = seqEmbeddedTodo.map {
          _.map(_.copy(categoryId = Category.Id(0)))
        }
        TodoRepository.bulkUpdate(seqUpdatedTodo)
    }
  }

}
