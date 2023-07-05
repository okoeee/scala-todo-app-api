package controllers.api

import forms.LoginForm
import ixias.play.api.auth.mvc.AuthExtensionMethods
import json.JsonResponse
import json.reads.JsValueLogin
import json.writes.JsValueAuthResponse
import mvc.auth.UserAuthProfile
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
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

  private val logger = Logger(this.getClass)

  def auth(): Action[AnyContent] = AuthenticatedOrNot(authProfile) {
    implicit req =>
      authProfile.loggedIn match {
        case Some(_) =>
          Ok(
            Json.toJson(
              JsValueAuthResponse(isLoggedIn = true, message = "Logged in")))
        case None =>
          // Ok(Json.toJson(JsValueAuthResponse(isLoggedIn = false, message = "Not logged in")))
          Ok(Json.obj("isLoggedIn" -> false, "message" -> "Not logged in"))
      }
  }

  def login(): Action[JsValue] = Action(parse.json).async { implicit req =>
    req.body
      .validate[JsValueLogin]
      .fold(
        errors => {
          Future.successful(JsonResponse.badRequest(errors.toString))
        },
        loginData => {
          UserService
            .login(LoginForm(loginData.email, loginData.password))
            .flatMap {
              case Left(msg) => Future.successful(JsonResponse.badRequest(msg))
              case Right(userId) =>
                authProfile.loginSucceeded(userId, { _ =>
                  JsonResponse.success("Logged in")
                }) recover {
                  case e: Exception =>
                    logger.error("ログインに失敗しました", e)
                    JsonResponse.internalServerError("ログインに失敗しました")
                }
            }
        }
      )
  }

}
