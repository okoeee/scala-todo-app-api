package controllers.api

import ixias.play.api.auth.mvc.AuthExtensionMethods
import json.writes.JsValueAuthResponse
import mvc.auth.UserAuthProfile
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
  val controllerComponents: ControllerComponents,
  val authProfile: UserAuthProfile
)(implicit ec: ExecutionContext)
  extends AuthExtensionMethods
  with BaseController
  with play.api.i18n.I18nSupport {

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

}
