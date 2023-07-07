package controllers.api

import forms.LoginForm
import ixias.play.api.auth.mvc.AuthExtensionMethods
import json.JsonResponse
import json.reads.JsValueLogin
import json.writes.{JsValueAuthResponse, JsValueUser}
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

  /**
    * セッションの認証を行う
    */
  def verify() = AuthenticatedOrNot(authProfile) { implicit req =>
    authProfile.loggedIn match {
      case Some(_) =>
        logger.info("authProfile.loggedIn success")
        Ok(
          Json.toJson(
            JsValueAuthResponse(isLoggedIn = true, message = "Logged in")))
      case None =>
        logger.info("authProfile.loggedIn failed")
        Ok(
          Json.toJson(
            JsValueAuthResponse(isLoggedIn = false, message = "Not logged in")))
    }
  }

  def login() = Action(parse.json).async { implicit req =>
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
                  logger.info("ログインに成功しました")
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

//  def logout() = Authenticated(authProfile).async { implicit req =>
//    authProfile.logoutSucceeded { _ =>
//      logger.info("ログアウトに成功しました")
//      JsonResponse.success("Logged out")
//    } recover {
//      case e: Exception =>
//        logger.error("ログアウトに失敗しました", e)
//        JsonResponse.internalServerError("ログアウトに失敗しました")
//    }
//  }

  def show(): Action[AnyContent] = Authenticated(authProfile) { implicit req =>
    authProfile.loggedIn match {
      case Some(user) =>
        logger.info("UserController.show success")
        Ok(Json.toJson(JsValueUser(user.id, user.v.name, user.v.email)))
      case None =>
        logger.info("UserController.show failed")
        JsonResponse.badRequest("Not logged in")
    }
  }

}
