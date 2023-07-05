package controllers.api

import json.writes.JsValueCategoryListItem
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.CategoryService

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  def index(): Action[AnyContent] = Action.async { implicit req =>
    CategoryService.getViewValueCategory.map { seqViewValueCategory =>
      val seqJsValueCategoryListItem = seqViewValueCategory.map {
        viewValueCategory =>
          JsValueCategoryListItem(viewValueCategory)
      }
      Ok(Json.toJson(seqJsValueCategoryListItem))
    }
  }

  def createAction(): Action[JsValue] = ???

}
