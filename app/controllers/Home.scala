/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._
import model.{ViewValueHome, ViewValueTodo}
import lib.persistence.onMySQL.{CategoryRepository, TodoRepository}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit req =>

    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css", "uikit.min.css"),
      jsSrc  = Seq("main.js")
    )

    val futureSeqViewValueTodo = (TodoRepository.getAll zip CategoryRepository.getAll).map { case (todos, categories) =>
      todos.flatMap { todo =>
        val t = todo.v
        val c = categories.find(_.id == t.categoryId).map(_.v)
        c.map { category =>
          ViewValueTodo(
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
      Ok(views.html.Home(vv, seqViewValueTodo))
    }

  }
}
