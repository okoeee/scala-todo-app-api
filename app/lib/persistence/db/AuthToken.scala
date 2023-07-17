package lib.persistence.db

import java.time.{Duration, LocalDateTime}
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table
import ixias.play.api.auth.token.Token.AuthenticityToken
import lib.model.{AuthToken, User}

case class AuthTokenTable[P <: JdbcProfile]()(implicit val driver: P)
  extends Table[AuthToken, P] {
  import api._

  lazy val dsn = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/to_do"),
    "slave" -> DataSourceName("ixias.db.mysql://slave/to_do")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "auth_token") {
    import AuthToken._
    // Columns
    /* @1 */
    def id = column[Id]("id", O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */
    def userId = column[User.Id]("user_id", O.UInt64)
    /* @3 */
    def token = column[AuthenticityToken]("token", O.Utf8Char255)
    /* @4 */
    def expiry = column[Option[Duration]]("expiry", O.Utf8Char255)
    /* @5 */
    def updatedAt = column[LocalDateTime]("updated_at", O.TsCurrent)
    /* @6 */
    def createdAt = column[LocalDateTime]("created_at", O.Ts)

    type TableElementTuple = (
      Option[Id],
      User.Id,
      AuthenticityToken,
      Option[Duration],
      LocalDateTime,
      LocalDateTime
    )

    def * = (id.?, userId, token, expiry, updatedAt, createdAt) <> (
      // Tuple(table) => Model
      (t: TableElementTuple) =>
        AuthToken(
          t._1,
          t._2,
          t._3,
          t._4,
          t._5,
          t._6
      ),
      // Model => Tuple(table)
      (v: TableElementType) =>
        AuthToken.unapply(v).map { t =>
          (
            t._1,
            t._2,
            t._3,
            t._4,
            LocalDateTime.now(),
            t._5
          )
      }
    )
  }
}
