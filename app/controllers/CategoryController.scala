package controllers

import forms.CategoryForm.categoryForm
import lib.model.Category
import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.CategoryRepository

import javax.inject._
import play.api.mvc._
import model.{ViewValueCategory, ViewValueHome}
import service.CategoryService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  private val vv = ViewValueHome(
    title = "Category",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  private val optionsOfCategoryColor = CategoryColor.values.tail.map {
    categoryColor =>
      (categoryColor.code.toString, categoryColor.name)
  }

  def index(): Action[AnyContent] = Action.async { implicit req =>
    CategoryService.getViewValueCategory.map { categories =>
      Ok(views.html.category.Index(vv, categories))
    }
  }

  def create: Action[AnyContent] = Action { implicit req =>
    Ok(views.html.category.Create(vv, categoryForm, optionsOfCategoryColor))
  }

  def createAction: Action[AnyContent] = Action.async { implicit req =>
    categoryForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            Ok(views.html.category
              .Create(vv, formWithErrors, optionsOfCategoryColor)))
        },
        todo => {
          val categoryWithNoId: Category#WithNoId = Category(
            name = todo.name,
            slug = todo.slug,
            categoryColor = todo.categoryColor
          )

          CategoryRepository.add(categoryWithNoId).map { _ =>
            Redirect(routes.CategoryController.index())
              .flashing("success" -> "Categoryを作成しました")
          }
        }
      )
  }

  def update(id: Long): Action[AnyContent] = ???

  def updateAction(id: Long): Action[AnyContent] = ???

  def removeAction(id: Long): Action[AnyContent] = ???

}
