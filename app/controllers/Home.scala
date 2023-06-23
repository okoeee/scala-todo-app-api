/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._
import model.ViewValueHome
import lib.persistence.onMySQL.TodoRepository
import responses.Todo

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit req =>

    TodoRepository.getAll.map{ todos =>
      val vv = ViewValueHome(
        title  = "Home",
        cssSrc = Seq("main.css"),
        jsSrc  = Seq("main.js")
      )

      val todosResponse = todos.map { todo =>
        Todo(todo.v.title)
      }

      Ok(views.html.Home(vv, todosResponse))
    }

  }
}
