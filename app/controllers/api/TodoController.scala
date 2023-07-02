package controllers.api

import json.reads.JsValueCreateTodo
import json.writes.JsValueTodoListItem
import lib.model.{Category, Todo}
import lib.persistence.onMySQL.TodoRepository
import model.ViewValueTodo
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.TodoService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  def index(): Action[AnyContent] = Action.async { implicit req =>
    TodoService.getSeqViewValueTodo.map { seqViewValueTodo =>
      val seqJsValue = seqViewValueTodo.map { viewValueTodo =>
        JsValueTodoListItem(viewValueTodo)
      }
      Ok(Json.toJson(seqJsValue))
    }
  }

  def show(id: Long): Action[AnyContent] = Action.async { implicit req =>
    TodoService.getViewValueTodo(Todo.Id(id)).map {
      case Left(msg) =>
        BadRequest(Json.obj("status" -> "error", "message" -> msg))
      case Right(viewValueTodo) =>
        Ok(Json.toJson(JsValueTodoListItem(viewValueTodo)))
    }
  }

  def createAction(): Action[JsValue] = Action(parse.json).async {
    implicit req =>
      req.body
        .validate[JsValueCreateTodo]
        .fold(
          errors => {
            val errMsg = JsError.toJson(errors).toString
            Future.successful(
              BadRequest(Json.obj("status" -> "error", "message" -> errMsg)))
          },
          todoData => {
            val todoWithNoId: Todo#WithNoId = Todo(
              categoryId = Category.Id(todoData.categoryId),
              title = todoData.title,
              body = todoData.body,
              state = Todo.Status.IS_STARTED
            )

            TodoRepository.add(todoWithNoId).map { _ =>
              Ok(Json.obj("status" -> "ok", "message" -> "Todoを作成しました"))
            }
          }
        )
  }

}
