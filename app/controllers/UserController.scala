package controllers

import forms.RegistrationForm.registrationForm
import ixias.play.api.auth.mvc.AuthExtensionMethods
import model.ViewValueHome
import mvc.auth.UserAuthProfile
import play.api.mvc.{BaseController, ControllerComponents}
import service.UserService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
  val controllerComponents: ControllerComponents,
  val authProfile: UserAuthProfile
)(implicit ec: ExecutionContext)
  extends AuthExtensionMethods
  with BaseController
  with play.api.i18n.I18nSupport {

  val vv = ViewValueHome(
    title = "ユーザー",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  def registration() = AuthenticatedOrNot(authProfile) { implicit req =>
    authProfile.loggedIn match {
      case Some(user) => Redirect(routes.HomeController.index())
      case None =>
        Ok(views.html.auth.Registration(vv, registrationForm))
    }
  }

  def registrationAction = Action.async { implicit req =>
    registrationForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            Ok(views.html.auth.Registration(vv, formWithErrors))
          )
        },
        registrationForm => {
          UserService.registration(registrationForm) flatMap {
            case Left(message) =>
              Future.successful(
                Redirect(routes.UserController.registration())
                  .flashing("error" -> message)
              )
            case Right(userId) =>
              authProfile.loginSucceeded(userId, { _ =>
                Redirect(routes.HomeController.index())
              })
          }
        }
      )
  }

}
