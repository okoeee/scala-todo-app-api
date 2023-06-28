package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL.TodoRepository

import javax.inject._
import play.api.mvc._
import model.ViewValueHome
import forms.TodoForm
import forms.TodoForm.todoForm
import service.{CategoryService, TodoService}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  private val vv = ViewValueHome(
    title = "Todo",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  private val optionsOfTodoStatus = Todo.Status.values.map { todoStatus =>
    (todoStatus.code.toString, todoStatus.name)
  }

  def index(): Action[AnyContent] = Action.async { implicit req =>
    val vv = ViewValueHome(
      title = "Home",
      cssSrc = Seq("uikit.min.css", "main.css"),
      jsSrc = Seq("uikit.min.js", "uikit-icons.min.js", "main.js")
    )

    TodoService.getFutureSeqViewValueTodo.map { seqViewValueTodo =>
      Ok(views.html.todo.Index(vv, seqViewValueTodo, todoForm))
    }
  }

  def create: Action[AnyContent] = Action.async { implicit req =>
    CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
      val defaultTodoForm = TodoForm(
        title = "",
        body = "",
        categoryId = Category.Id(optionsOfCategory.headOption.map(_._1.toLong).getOrElse(0L)),
        state = Todo.Status.IS_STARTED
      )
      Ok(
        views.html.todo
          .Create(
            vv,
            todoForm.fill(defaultTodoForm),
            optionsOfCategory,
            optionsOfTodoStatus
          ))
    }
  }

  def createAction: Action[AnyContent] = Action.async { implicit req =>
    todoForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
            Ok(
              views.html.todo.Create(
                vv,
                formWithErrors,
                optionsOfCategory,
                optionsOfTodoStatus
              ))
          }
        },
        todo => {
          val todoWithNoId: Todo#WithNoId = Todo(
            categoryId = Category.Id(todo.categoryId),
            title = todo.title,
            body = todo.body,
            state = Todo.Status.IS_STARTED
          )

          TodoRepository.add(todoWithNoId).map { _ =>
            Redirect(routes.TodoController.index())
              .flashing("success" -> "Todoを作成しました")
          } recover {
            case _: Exception =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todoの作成に失敗しました")
          }
        }
      )
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit req =>
    TodoRepository.get(Todo.Id(id)).flatMap {
      case None =>
        Future.successful(
          Redirect(routes.TodoController.index())
            .flashing("error" -> "更新対象のTodoが見つかりませんでした"))
      case Some(embeddedTodo) =>
        val preTodoForm = TodoForm(
          title = embeddedTodo.v.title,
          body = embeddedTodo.v.body,
          categoryId = embeddedTodo.v.categoryId,
          state = embeddedTodo.v.state
        )
        CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
          Ok(
            views.html.todo
              .Update(
                vv,
                todoForm.fill(preTodoForm),
                id,
                optionsOfCategory,
                optionsOfTodoStatus
              ))
        }
    }
  }

  def updateAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      todoForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
              Ok(
                views.html.todo
                  .Update(
                    vv,
                    formWithErrors,
                    id,
                    optionsOfCategory,
                    optionsOfTodoStatus
                  ))
            }
          },
          todo => {
            TodoRepository.get(Todo.Id(id)).flatMap {
              case None =>
                Future.successful(Redirect(routes.TodoController.index())
                  .flashing("error" -> "更新対象のTodoが見つかりませんでした"))
              case Some(embeddedTodo) =>
                val updatedTodo = embeddedTodo.map(
                  _.copy(
                    title = todo.title,
                    body = todo.body,
                    categoryId = Category.Id(todo.categoryId),
                    state = todo.state
                  ))

                TodoRepository.update(updatedTodo).map { _ =>
                  Redirect(routes.TodoController.index())
                    .flashing("success" -> "Todoを更新しました")
                } recover {
                  case _: Exception =>
                    Redirect(routes.TodoController.index())
                      .flashing("error" -> "Todoの更新に失敗しました")
                }
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      TodoRepository.get(Todo.Id(id)).flatMap {
        case None =>
          Future.successful(
            Redirect(routes.TodoController.index())
              .flashing("error" -> "削除対象のTodoが見つかりませんでした"))
        case Some(embeddedTodo) =>
          TodoRepository.remove(embeddedTodo.id).map { _ =>
            Redirect(routes.TodoController.index())
              .flashing("success" -> "Todoを削除しました")
          } recover {
            case _: Exception =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todoの削除に失敗しました")
          }
      }
  }

}
