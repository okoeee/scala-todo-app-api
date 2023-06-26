
package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL.TodoRepository

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.{ViewValueHome, ViewValueTodo}
import play.api.data.Form
import requests.TodoForm

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with play.api.i18n.I18nSupport {

  private val todoForm: Form[TodoForm] = Form(
    mapping(
      "title" -> nonEmptyText.verifying(
        "タイトルは英数字・日本語を入力することができ、改行を含むことができません",
        title => title.matches("([^\\x01-\\x7E]|\\w)+") && !title.contains("\n")
      ),
      "body" -> nonEmptyText.verifying(
        "本文は英数字・日本語のみを入力することができます",
        body => body.matches("([^\\x01-\\x7E]|\\w)+")
      ),
      "category" -> shortNumber.verifying(
        "登録されているカテゴリーのみを入力してください",
        category => selectValues.exists{case (code, _) => code.toShort == category}
      )
    )(TodoForm.apply)(TodoForm.unapply)
  )

  private val vv = ViewValueHome(
    title = "Todo Create",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  private val cc = Category.CategoryColor
  private val selectValues = Seq(
    (cc.IS_FRONTEND.code.toString, cc.IS_FRONTEND.name),
    (cc.IS_BACKEND.code.toString, cc.IS_BACKEND.name),
    (cc.IS_INFRA.code.toString, cc.IS_INFRA.name),
  )

  def create: Action[AnyContent] = Action { implicit req =>
    Ok(views.html.todo.Create(vv, todoForm, selectValues))
  }

  def createAction: Action[AnyContent] = Action.async { implicit req =>
    todoForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(Ok(views.html.todo.Create(vv, formWithErrors, selectValues)))
      },
      todo => {
        val todoWithNoId: Todo#WithNoId = Todo(
          categoryId = todo.category,
          title = todo.title,
          body = todo.body,
          state = Todo.Status.IS_STARTED
        )

        TodoRepository.add(todoWithNoId).map { _ =>
          Redirect(routes.HomeController.index)
        }
      }
    )
  }

}
