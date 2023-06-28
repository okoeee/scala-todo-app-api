package controllers

import ixias.model.{@@, tag}
import lib.model.{Category, Todo}
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}

import javax.inject._
import play.api.mvc._
import play.api.data.Forms._
import model.{ViewValueHome, ViewValueTodo}
import play.api.data.{Form, FormError}
import play.api.data.format.{Formats, Formatter}
import requests.TodoForm
import service.CategoryService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  implicit val categoryIdFormat: Formatter[Category.Id] =
    new Formatter[Category.Id] {
      override def bind(
        key: String,
        data: Map[String, String]): Either[Seq[FormError], Category.Id] = {
        data.get(key) match {
          case Some(s) =>
            try {
              Right(tag[Category.Id](s.toLong).asInstanceOf[Category.Id])
            } catch {
              case _: NumberFormatException =>
                Left(Seq(FormError(key, "error.number", Nil)))
            }
          case None => Left(Seq(FormError(key, "error.required", Nil)))
        }
      }

      override def unbind(key: String,
                          value: Category.Id): Map[String, String] =
        Map(key -> value.toString)
    }

  implicit val todoStatusFormat = new Formatter[Todo.Status] {
    override def bind(
      key: String,
      data: Map[String, String]): Either[Seq[FormError], Todo.Status] = {
      data.get(key) match {
        case Some(str) =>
          try {
            Right(
              Todo.Status
                .find(_.code == str.toShort)
                .getOrElse(Todo.Status.IS_STARTED))
          } catch {
            case _: NumberFormatException =>
              Left(Seq(FormError(key, "error.required", Nil)))
          }
        case None => Left(Seq(FormError(key, "error.required", Nil)))
      }
    }
    override def unbind(key: String, value: Todo.Status): Map[String, String] =
      Map(key -> value.toString)
  }

  private val todoForm: Form[TodoForm] = Form(
    mapping(
      "title" -> nonEmptyText.verifying(
        "タイトルは英数字・日本語を入力することができ、改行を含むことができません",
        title =>
          title
            .matches("[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+") && !title
            .contains("\n")
      ),
      "body" -> nonEmptyText.verifying(
        "本文は英数字・日本語のみを入力することができます",
        body =>
          body.matches(
            "[\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}\\r\\n]+")
      ),
      "categoryId" -> of[Category.Id],
      "state" -> of[Todo.Status]
    )(TodoForm.apply)(TodoForm.unapply)
  )

  private val vv = ViewValueHome(
    title = "Todo Create",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  private val ts = Todo.Status
  // selectボックスのOptionの値
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

    val futureSeqViewValueTodo =
      (TodoRepository.getAll zip CategoryRepository.getAll).map {
        case (todos, categories) =>
          todos.flatMap { embeddedTodo =>
            val todo = embeddedTodo.v
            val optCategory = categories.find(_.id == todo.categoryId).map(_.v)
            optCategory.map { category =>
              ViewValueTodo(
                id = embeddedTodo.id,
                title = todo.title,
                body = todo.body,
                status = todo.state,
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

  def create: Action[AnyContent] = Action.async { implicit req =>
    val defaultTodoForm = TodoForm(
      title = "",
      body = "",
      categoryId = Category.Id(1),
      state = Todo.Status.IS_STARTED
    )
    CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
      Ok(
        views.html.todo
          .Create(
            vv,
            todoForm.fill(defaultTodoForm),
            optionsOfCategory,
            optionsOfTodoStatus
          ))
    }
  }

  def createAction: Action[AnyContent] = Action.async { implicit req =>
    todoForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
            Ok(
              views.html.todo.Create(
                vv,
                formWithErrors,
                optionsOfCategory,
                optionsOfTodoStatus
              ))
          }
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
              .flashing("success" -> "Todoを作成しました")
          } recover {
            case _: Exception =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todoの作成に失敗しました")
          }
        }
      )
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit req =>
    TodoRepository.get(Todo.Id(id)).flatMap {
      case None =>
        Future.successful(
          Redirect(routes.TodoController.index())
            .flashing("error" -> "更新対象のTodoが見つかりませんでした"))
      case Some(embeddedTodo) =>
        val preTodoForm = TodoForm(
          title = embeddedTodo.v.title,
          body = embeddedTodo.v.body,
          categoryId = embeddedTodo.v.categoryId,
          state = embeddedTodo.v.state
        )
        CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
          Ok(
            views.html.todo
              .Update(
                vv,
                todoForm.fill(preTodoForm),
                id,
                optionsOfCategory,
                optionsOfTodoStatus
              ))
        }
    }
  }

  def updateAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      todoForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            CategoryService.getOptionsOfCategory.map { optionsOfCategory =>
              Ok(
                views.html.todo
                  .Update(
                    vv,
                    formWithErrors,
                    id,
                    optionsOfCategory,
                    optionsOfTodoStatus
                  ))
            }
          },
          todo => {
            TodoRepository.get(Todo.Id(id)).flatMap {
              case None =>
                Future.successful(Redirect(routes.TodoController.index())
                  .flashing("error" -> "更新対象のTodoが見つかりませんでした"))
              case Some(embeddedTodo) =>
                val updatedTodo = embeddedTodo.map(
                  _.copy(
                    title = todo.title,
                    body = todo.body,
                    categoryId = Category.Id(todo.categoryId),
                    state = todo.state
                  ))

                TodoRepository.update(updatedTodo).map { _ =>
                  Redirect(routes.TodoController.index())
                    .flashing("success" -> "Todoを更新しました")
                } recover {
                  case _: Exception =>
                    Redirect(routes.TodoController.index())
                      .flashing("error" -> "Todoの更新に失敗しました")
                }
            }
          }
        )
  }

  def removeAction(id: Long): Action[AnyContent] = Action.async {
    implicit req =>
      TodoRepository.get(Todo.Id(id)).flatMap {
        case None =>
          Future.successful(
            Redirect(routes.TodoController.index())
              .flashing("error" -> "削除対象のTodoが見つかりませんでした"))
        case Some(embeddedTodo) =>
          TodoRepository.remove(embeddedTodo.id).map { _ =>
            Redirect(routes.TodoController.index())
              .flashing("success" -> "Todoを削除しました")
          } recover {
            case _: Exception =>
              Redirect(routes.TodoController.index())
                .flashing("error" -> "Todoの削除に失敗しました")
          }
      }
  }

}
