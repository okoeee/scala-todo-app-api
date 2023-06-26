
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

    val futureSeqViewValueTodo = for {
      todos <- TodoRepository.getAll
      categories <- CategoryRepository.getAll
    } yield {
      todos.map { todo =>
        val t = todo.v
        val c = categories.find(_.id == t.categoryId).getOrElse(throw new NoSuchElementException).v
        ViewValueTodo(
          title = t.title,
          body = t.body,
          status = t.state,
          categoryName = c.name,
          categoryColor = c.categoryColor
        )
      }
    }

    futureSeqViewValueTodo.map { seqViewValueTodo =>
      Ok(views.html.Home(vv, seqViewValueTodo))
    }

  }
}
