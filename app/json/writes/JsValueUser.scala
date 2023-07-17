package json.writes

import lib.model.User
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Writes}

case class JsValueUser(
  id: Long,
  name: String,
  email: String
)

object JsValueUser {

  implicit val writes: Writes[JsValueUser] = Json.writes[JsValueUser]

}
