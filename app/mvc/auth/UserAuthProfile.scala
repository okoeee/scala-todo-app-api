package mvc.auth

import ixias.play.api.auth.container.Container
import ixias.play.api.auth.mvc.AuthProfile
import ixias.play.api.auth.token.Token
import ixias.play.api.auth.token.Token.AuthenticityToken
import ixias.play.api.auth.token.TokenViaSession
import lib.model.User
import lib.persistence.onMySQL.UserRepository
import play.api.Environment
import play.api.mvc.{RequestHeader, Result}

import java.time.Duration
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class UserAuthProfile @Inject()(
  val container: AuthTokenContainer
)(implicit ec: ExecutionContext)
  extends AuthProfile[User.Id, User, Unit] {

  val env: Environment = Environment.simple()
  val tokenAccessor: Token = TokenViaSession("user")
  val datastore: Container[Id] = container
  val executionContext: ExecutionContext = ec

  override def resolve(id: Id)(
    implicit rh: RequestHeader): Future[Option[AuthEntity]] =
    UserRepository.get(id)

  override def sessionTimeout(
    implicit request: RequestHeader): Option[Duration] =
    Some(Duration.ofHours(24L))

//  override def loginSucceeded(id: Id, block: AuthenticityToken => Result)(
//    implicit rh: RequestHeader): Future[Result] =
//    for {
//      token <- datastore.open(id, sessionTimeout)
//    } yield tokenAccessor.put(token)(block(token))

}
