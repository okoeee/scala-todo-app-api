package json.writes

import play.api.libs.json.{Json, Writes}

case class JsValueAuthResponse(
  isLoggedIn: Boolean,
  message: String
)

object JsValueAuthResponse {
  implicit val writes: Writes[JsValueAuthResponse] =
    Json.writes[JsValueAuthResponse]
}
