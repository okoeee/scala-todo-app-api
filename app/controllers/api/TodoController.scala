package controllers.api

import json.JsonResponse
import json.reads.{JsValueCreateTodo, JsValueUpdateTodo}
import json.writes.JsValueTodoListItem
import lib.model.Todo
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
    TodoService.getAll.map { seqViewValueTodo =>
      val seqJsValue = seqViewValueTodo.map { viewValueTodo =>
        JsValueTodoListItem(viewValueTodo)
      }
      Ok(Json.toJson(seqJsValue))
    }
  }

  def show(id: Long): Action[AnyContent] = Action.async { implicit req =>
    TodoService.get(Todo.Id(id)).map {
      case Left(msg) =>
        JsonResponse.badRequest(msg)
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
            Future.successful(JsonResponse.badRequest(errMsg))
          },
          todoData => {
            TodoService.create(todoData).map {
              case Left(msg) =>
                JsonResponse.badRequest(msg)
              case Right(msg) =>
                JsonResponse.success(msg)
            }
          }
        )
  }

  def updateAction(id: Long): Action[JsValue] = Action(parse.json).async {
    implicit req =>
      req.body
        .validate[JsValueUpdateTodo]
        .fold(
          errors => {
            val errMsg = JsError.toJson(errors).toString
            Future.successful(JsonResponse.badRequest(errMsg))
          },
          todoData => {
            TodoService.update(Todo.Id(id), todoData).map {
              case Left(msg) =>
                JsonResponse.badRequest(msg)
              case Right(msg) =>
                JsonResponse.success(msg)
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      TodoService.remove(Todo.Id(id)).map {
        case Left(msg) =>
          BadRequest(Json.obj("status" -> "error", "message" -> msg))
        case Right(msg) => Ok(Json.obj("status" -> "ok", "message" -> msg))
      }
  }
}
