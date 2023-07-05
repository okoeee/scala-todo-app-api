package json

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError, Ok}

object JsonResponse {

  def success(message: String): Result =
    Ok(Json.obj("status" -> "ok", "message" -> message))

  def badRequest(message: String): Result =
    BadRequest(Json.obj("status" -> "error", "message" -> message))

  def internalServerError(message: String) =
    InternalServerError(Json.obj("status" -> "error", "message" -> message))

}
