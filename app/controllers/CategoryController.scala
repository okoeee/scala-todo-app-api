package controllers

import forms.CategoryForm
import forms.CategoryForm.categoryForm
import ixias.play.api.auth.mvc.AuthExtensionMethods
import lib.model.Category
import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}

import javax.inject._
import play.api.mvc._
import model.{ViewValueCategory, ViewValueHome}
import mvc.auth.UserAuthProfile
import service.{CategoryService, TodoService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents,
  val authProfile: UserAuthProfile
)(implicit ec: ExecutionContext)
  extends AuthExtensionMethods
  with BaseController
  with play.api.i18n.I18nSupport {

  private val vv = ViewValueHome(
    title = "Category",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  // CategoryColor.COLOR_OPTION_NONEはTodoにcategoryIdが設定されていない(categoryId=0)ときに使用するのため、selectフォーム用のoptionでは除いている
  private val optionsOfCategoryColor = CategoryColor.values
    .filterNot(_ == CategoryColor.COLOR_OPTION_NONE)
    .map { categoryColor =>
      (categoryColor.code.toString, categoryColor.name)
    }

  def index(): Action[AnyContent] = AuthenticatedOrNot(authProfile).async {
    implicit req =>
      authProfile.loggedIn match {
        case Some(_) =>
          val indexVv =
            vv.copy(
              jsSrc = Seq("uikit.min.js", "uikit-icons.min.js", "main.js"))

          CategoryService.getViewValueCategory.map { categories =>
            Ok(views.html.category.Index(indexVv, categories))
          }
        case None =>
          Future.successful(
            Redirect(routes.UserController.login())
              .flashing("error" -> "ログインもしくは会員登録をしてください"))
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

  def update(id: Long): Action[AnyContent] = Action.async { implicit req =>
    CategoryRepository.get(Category.Id(id)).map {
      case None =>
        Redirect(routes.CategoryController.index())
          .flashing("error" -> "更新対象のカテゴリーが見つかりませんでした")
      case Some(embeddedCategory) =>
        val preCategoryForm = CategoryForm(
          name = embeddedCategory.v.name,
          slug = embeddedCategory.v.slug,
          categoryColor = embeddedCategory.v.categoryColor
        )
        Ok(
          views.html.category.Update(
            vv,
            id,
            categoryForm.fill(preCategoryForm),
            optionsOfCategoryColor
          ))
    }
  }

  def updateAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      categoryForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              Ok(
                views.html.category
                  .Update(
                    vv,
                    id,
                    formWithErrors,
                    optionsOfCategoryColor,
                  )))
          },
          category => {
            CategoryRepository.get(Category.Id(id)).flatMap {
              case None =>
                Future.successful(Redirect(routes.CategoryController.index())
                  .flashing("error" -> "更新対象のCategoryが見つかりませんでした"))
              case Some(embeddedCategory) =>
                val updatedCategory = embeddedCategory.map(
                  _.copy(
                    name = category.name,
                    slug = category.slug,
                    categoryColor = category.categoryColor
                  ))

                CategoryRepository.update(updatedCategory).map { _ =>
                  Redirect(routes.CategoryController.index())
                    .flashing("success" -> "Categoryを更新しました")
                } recover {
                  case _: Exception =>
                    Redirect(routes.CategoryController.index())
                      .flashing("error" -> "Categoryの更新に失敗しました")
                }
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      CategoryRepository.get(Category.Id(id)).flatMap {
        case None =>
          Future.successful(
            Redirect(routes.CategoryController.index())
              .flashing("error" -> "削除対象のカテゴリーが見つかりませんでした"))
        case Some(embeddedCategory) =>
          CategoryRepository.remove(embeddedCategory.id).flatMap {
            case None =>
              Future.successful(Redirect(routes.CategoryController.index())
                .flashing("error" -> "Categoryの削除に失敗しました"))
            case Some(embeddedCategory) =>
              TodoService.updateTodosOfNoneCategory(embeddedCategory.id).map {
                _ =>
                  Redirect(routes.CategoryController.index())
                    .flashing("success" -> "Categoryを削除しました")
              }
          } recover {
            case _: Exception =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Categoryの削除に失敗しました")
          }
      }
  }

}
