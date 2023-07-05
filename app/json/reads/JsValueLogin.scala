package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueLogin(
  email: String,
  password: String
)

object JsValueLogin {
  implicit val reads: Reads[JsValueLogin] = Json.reads[JsValueLogin]
}
