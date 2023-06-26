/**
 *
 * to do sample project
 *
 */

package controllers

import lib.model.Category

import javax.inject._
import play.api.mvc._
import model.ViewValueHome
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}
import responses.Todo

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit req =>

    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    CategoryRepository.getAll.map { cs =>
      cs.map(_.v.name).foreach(println(_))
    }

    TodoRepository.getAll.map { todos =>
      val todosResponse = todos.map { todo =>
        Todo(todo.v.title)
      }
      Ok(views.html.Home(vv, todosResponse))
    }

  }
}
