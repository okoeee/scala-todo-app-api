package lib.persistence

import scala.concurrent.Future
import ixias.persistence.SlickRepository
import ixias.play.api.auth.token.Token.AuthenticityToken
import lib.model.{AuthToken, User}
import slick.jdbc.JdbcProfile

case class AuthTokenRepository[P <: JdbcProfile]()(implicit val driver: P)
  extends SlickRepository[AuthToken.Id, AuthToken, P]
  with db.SlickResourceProvider[P] {

  import api._

  /**
    * Get AuthToken Data
    */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(AuthTokenTable, "slave") {
      _.filter(_.id === id).result.headOption
    }

  /**
    * Get AuthToken By User Id
    */
  def getByUserId(userId: User.Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(AuthTokenTable, "slave") {
      _.filter(_.userId === userId).result.headOption
    }

  /**
    * Get AuthToken By Token
    */
  def getByToken(token: AuthenticityToken): Future[Option[EntityEmbeddedId]] =
    RunDBAction(AuthTokenTable, "slave") {
      _.filter(_.token === token).result.headOption
    }

  /**
    * Add AuthToken Data
    */
  def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(AuthTokenTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  /**
    * Update AuthToken Data
    */
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(AuthTokenTable) { slick =>
      val row = slick.filter(_.id === entity.id)
      for {
        old <- row.result.headOption
        _ <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.update(entity.v)
        }
      } yield old
    }

  /**
    * Delete AuthToken Data
    */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(AuthTokenTable) { slick =>
      val row = slick.filter(_.id === id)
      for {
        old <- row.result.headOption
        _ <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.delete
        }
      } yield old
    }
}
