package mvc.auth

import ixias.play.api.auth.container.Container
import ixias.play.api.auth.token.Token.AuthenticityToken
import ixias.security.TokenGenerator
import lib.model.{AuthToken, User}
import lib.persistence.onMySQL.AuthTokenRepository
import play.api.mvc.RequestHeader

import java.time.Duration
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class AuthTokenContainer @Inject()()(implicit ec: ExecutionContext)
  extends Container[User.Id] {

  val TOKEN_LENGTH = 30
  val executionContext: ExecutionContext = ec

  override def open(userId: Id, expiry: Option[Duration])(
    implicit request: RequestHeader): Future[AuthenticityToken] = {
    AuthTokenRepository.getByUserId(userId).flatMap {
      case Some(authToken) => Future.successful(authToken.v.token)
      case None => {
        val token = AuthenticityToken(TokenGenerator().next(TOKEN_LENGTH))
        val authToken = AuthToken(userId, token, expiry)
        AuthTokenRepository.add(authToken).map(_ => token)
      }
    }
  }

  override def setTimeout(token: AuthenticityToken, expiry: Option[Duration])(
    implicit request: RequestHeader): Future[Unit] = {
    AuthTokenRepository.getByToken(token).map {
      case Some(authToken) => {
        val newAuthToken = authToken.map(_.copy(expiry = expiry))
        AuthTokenRepository.update(newAuthToken)
      }
      case None => ()
    }
  }

  override def read(token: AuthenticityToken)(
    implicit request: RequestHeader): Future[Option[Id]] = {
    AuthTokenRepository.getByToken(token).map {
      case Some(authToken) => Some(authToken.v.userId)
      case None            => None
    }
  }

  override def destroy(token: AuthenticityToken)(
    implicit request: RequestHeader): Future[Unit] = {
    AuthTokenRepository.getByToken(token).map {
      case Some(authToken) => AuthTokenRepository.remove(authToken.id)
      case None            => ()
    }
  }

}
