package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}

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
class TodoController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

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
      "categoryId" -> longNumber.verifying(
        "登録されているカテゴリーのみを入力してください",
        categoryId =>
          optionsOfCategoryId.exists {
            case (code, _) => code.toLong == categoryId
        }
      ),
      "state" -> shortNumber.verifying(
        "登録されているステータスのみを入力してください",
        state =>
          optionsOfTodoStatus.exists {
            case (strCode, _) => strCode.toShort == state
        }
      )
    )(TodoForm.apply)(TodoForm.unapply)
  )

  private val vv = ViewValueHome(
    title = "Todo Create",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  private val cc = Category.CategoryColor
  private val optionsOfCategoryId = Seq(
    (cc.IS_FRONTEND.code.toString, cc.IS_FRONTEND.name),
    (cc.IS_BACKEND.code.toString, cc.IS_BACKEND.name),
    (cc.IS_INFRA.code.toString, cc.IS_INFRA.name),
  )

  private val ts = Todo.Status
  private val optionsOfTodoStatus = Seq(
    (ts.IS_STARTED.code.toString, ts.IS_STARTED.name),
    (ts.IS_PROGRESSIVE.code.toString, ts.IS_PROGRESSIVE.name),
    (ts.IS_COMPLETED.code.toString, ts.IS_COMPLETED.name)
  )

  def index(): Action[AnyContent] = Action.async { implicit req =>
    val vv = ViewValueHome(
      title = "Home",
      cssSrc = Seq("uikit.min.css", "main.css"),
      jsSrc = Seq("uikit.min.js", "uikit-icons.min.js", "main.js")
    )

    val futureSeqViewValueTodo = (TodoRepository.getAll zip CategoryRepository.getAll).map { case (todos, categories) =>
      todos.flatMap { todo =>
        val t = todo.v
        val c = categories.find(_.id == t.categoryId).map(_.v)
        c.map { category =>
          ViewValueTodo(
            id = todo.id,
            title = t.title,
            body = t.body,
            status = t.state,
            categoryName = category.name,
            categoryColor = category.categoryColor
          )
        }
      }
    }
    futureSeqViewValueTodo.map { seqViewValueTodo =>
      Ok(views.html.todo.Index(vv, seqViewValueTodo, todoForm))
    }
  }

  def create: Action[AnyContent] = Action { implicit req =>
    val defaultTodoForm = TodoForm(
      title = "",
      body = "",
      categoryId = 1,
      state = 0
    )
    Ok(
      views.html.todo
        .Create(vv, todoForm.fill(defaultTodoForm), optionsOfCategoryId, optionsOfTodoStatus))
  }

  def createAction: Action[AnyContent] = Action.async { implicit req =>
    todoForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            Ok(
              views.html.todo.Create(
                vv,
                formWithErrors,
                optionsOfCategoryId,
                optionsOfTodoStatus
              )))
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
          }
        }
      )
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit req =>
    TodoRepository.get(Todo.Id(id)).flatMap { optTodo =>
      optTodo
        .map { todo =>
          val preTodoForm = TodoForm(
            title = todo.v.title,
            body = todo.v.body,
            categoryId = todo.v.categoryId,
            state = todo.v.state.code
          )
          Future.successful(
            Ok(
              views.html.todo
                .Update(
                  vv,
                  todoForm.fill(preTodoForm),
                  id,
                  optionsOfCategoryId,
                  optionsOfTodoStatus
                )))
        }
        .getOrElse {
          Future.successful(Redirect(routes.TodoController.index()))
        }
    }
  }

  def updateAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      todoForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              Ok(
                views.html.todo
                  .Update(
                    vv,
                    formWithErrors,
                    id,
                    optionsOfCategoryId,
                    optionsOfTodoStatus
                  )))
          },
          todo => {
            TodoRepository.get(Todo.Id(id)).flatMap {
              case None =>
                Future.successful(Redirect(routes.TodoController.index()))
              case Some(embeddedTodo) =>
                val updatedTodo = embeddedTodo.map(
                  _.copy(
                    title = todo.title,
                    body = todo.body,
                    categoryId = Category.Id(todo.categoryId),
                    state = Todo.Status(code = todo.state)
                  ))

                TodoRepository.update(updatedTodo).map { _ =>
                  Redirect(routes.TodoController.index())
                }
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      TodoRepository.get(Todo.Id(id)).flatMap {
        case None =>
          Future.successful(Redirect(routes.TodoController.index()))
        case Some(embeddedTodo) =>
          TodoRepository.remove(embeddedTodo.id).map { _ =>
            Redirect(routes.TodoController.index())
          }
      }
  }

}
