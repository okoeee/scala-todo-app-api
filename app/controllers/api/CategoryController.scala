package controllers.api

import json.JsonResponse
import json.reads.JsValueCreateCategory
import json.writes.JsValueCategoryListItem
import lib.model.Category
import play.api.libs.json.{JsError, JsValue, Json}
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

  def show(id: Long): Action[AnyContent] = Action.async { implicit req =>
    CategoryService.get(Category.Id(id)).map {
      case Left(msg) =>
        JsonResponse.badRequest(msg)
      case Right(viewValueCategory) =>
        Ok(Json.toJson(JsValueCategoryListItem(viewValueCategory)))
    }
  }

  def createAction(): Action[JsValue] = Action(parse.json).async {
    implicit req =>
      req.body
        .validate[JsValueCreateCategory]
        .fold(
          errors => {
            val errMsg = JsError.toJson(errors).toString
            Future.successful(JsonResponse.badRequest(errMsg))
          },
          categoryData => {
            CategoryService.add(categoryData).map {
              case Left(msg)  => JsonResponse.badRequest(msg)
              case Right(msg) => JsonResponse.success(msg)
            }
          }
        )
  }

  def updateAction(id: Long): Action[JsValue] = Action(parse.json).async {
    implicit req =>
      req.body
        .validate[JsValueCreateCategory]
        .fold(
          errors => {
            val errMsg = JsError.toJson(errors).toString
            Future.successful(JsonResponse.badRequest(errMsg))
          },
          categoryData => {
            CategoryService.update(Category.Id(id), categoryData).map {
              case Left(msg)  => JsonResponse.badRequest(msg)
              case Right(msg) => JsonResponse.success(msg)
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      CategoryService.remove(Category.Id(id)).map {
        case Left(msg)  => JsonResponse.badRequest(msg)
        case Right(msg) => JsonResponse.success(msg)
      }
  }

}
