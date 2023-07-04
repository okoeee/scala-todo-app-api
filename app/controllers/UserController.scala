package controllers

import forms.LoginForm.loginForm
import forms.RegistrationForm.registrationForm
import ixias.play.api.auth.mvc.AuthExtensionMethods
import model.ViewValueHome
import mvc.auth.UserAuthProfile
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
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
    title = "",
    cssSrc = Seq("uikit.min.css", "main.css"),
    jsSrc = Seq("main.js")
  )

  def registration() = AuthenticatedOrNot(authProfile) { implicit req =>
    authProfile.loggedIn match {
      case Some(user) => Redirect(routes.HomeController.index())
      case None =>
        Ok(
          views.html.auth.Registration(
            vv.copy(title = "会員登録"),
            registrationForm
          ))
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

  def login(): Action[AnyContent] = AuthenticatedOrNot(authProfile) {
    implicit req =>
      authProfile.loggedIn match {
        case Some(_) => Redirect(routes.HomeController.index())
        case None =>
          Ok(views.html.auth.Login(vv.copy(title = "ログイン"), loginForm))
      }
  }

  def loginAction(): Action[AnyContent] = Action.async { implicit req =>
    loginForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            Ok(views.html.auth.Login(vv.copy(title = "ログイン"), formWithErrors))
          )
        },
        loginForm =>
          UserService.login(loginForm).flatMap {
            case Left(message) =>
              Future.successful(
                Redirect(routes.UserController.login())
                  .flashing("error" -> message)
              )
            case Right(userId) =>
              authProfile.loginSucceeded(userId, { _ =>
                Redirect(routes.HomeController.index())
              })
        }
      )
  }

  def logout(): Action[AnyContent] = Authenticated(authProfile).async {
    implicit req =>
      authProfile.loggedIn { user =>
        authProfile.logoutSucceeded(user.id, {
          Redirect(routes.UserController.login())
        })
      }
  }

}
