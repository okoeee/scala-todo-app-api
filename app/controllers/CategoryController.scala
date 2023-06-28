package controllers

import lib.persistence.onMySQL.CategoryRepository

import javax.inject._
import play.api.mvc._
import model.{ViewValueCategory, ViewValueHome}
import service.CategoryService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CategoryController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
    with play.api.i18n.I18nSupport {

  private val vv = ViewValueHome(
    title = "Category",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  def index(): Action[AnyContent] = Action.async { implicit req =>
    CategoryService.getViewValueCategory.map { categories =>
      Ok(views.html.category.Index(vv, categories))
    }
  }

  def create: Action[AnyContent] = ???

  def createAction: Action[AnyContent] = ???

  def update(id: Long): Action[AnyContent] = ???

  def updateAction(id: Long): Action[AnyContent] = ???

  def removeAction(id: Long): Action[AnyContent] = ???

}
