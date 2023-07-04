package lib.model

import ixias.model._

import java.time.{Duration, LocalDateTime}
import AuthToken._
import ixias.play.api.auth.token.Token.AuthenticityToken

case class AuthToken(
  id: Option[Id],
  uid: User.Id,
  token: AuthenticityToken,
  expiry: Option[Duration] = None,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

object AuthToken {

  val Id = the[Identity[Id]]
  type Id = Long @@ AuthToken
  type WithNoId = Entity.WithNoId[Id, AuthToken]
  type EmbeddedId = Entity.EmbeddedId[Id, AuthToken]

  def apply(
    uid: User.Id,
    token: AuthenticityToken,
    expiry: Option[Duration]
  ): AuthToken#WithNoId =
    new AuthToken(
      id = None,
      uid = uid,
      token = token,
      expiry = expiry
    ).toWithNoId

}
