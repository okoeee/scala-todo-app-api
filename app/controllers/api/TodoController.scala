package controllers.api

import json.writes.JsValueTodoListItem
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.TodoService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
    with play.api.i18n.I18nSupport {

  def index(): Action[AnyContent] = Action.async { implicit req =>
    TodoService.getSeqViewValueTodo.map { seqViewValueTodo =>
      val seqJsValue = seqViewValueTodo.map { viewValueTodo => JsValueTodoListItem(viewValueTodo) }
      Ok(Json.toJson(seqJsValue))
    }
  }

}
