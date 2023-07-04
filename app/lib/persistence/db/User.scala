package lib.persistence.db

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table

import lib.model.User

case class UserTable[P <: JdbcProfile]()(implicit val driver: P)
  extends Table[User, P] {
  import api._

  lazy val dsn = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/user"),
    "slave" -> DataSourceName("ixias.db.mysql://slave/user")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "user") {
    import User._
    // Columns
    /* @1 */
    def id = column[Id]("id", O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */
    def name = column[String]("name", O.Utf8Char255)
    /* @3 */
    def email = column[String]("email", O.Utf8Char255)
    /* @4 */
    def password = column[String]("password", O.Utf8Char255)
    /* @5 */
    def updatedAt = column[LocalDateTime]("updated_at", O.TsCurrent)
    /* @6 */
    def createdAt = column[LocalDateTime]("created_at", O.Ts)

    type TableElementTuple = (
      Option[Id],
      String,
      String,
      String,
      LocalDateTime,
      LocalDateTime
    )

    def * = (id.?, name, email, password, updatedAt, createdAt) <> (
      // Tuple(table) => Model
      (t: TableElementTuple) =>
        User(
          t._1,
          t._2,
          t._3,
          t._4,
          t._5,
          t._6
      ),
      // Model => Tuple(table)
      (v: TableElementType) =>
        User.unapply(v).map { t =>
          (
            t._1,
            t._2,
            t._3,
            t._4,
            LocalDateTime.now(),
            t._6
          )
      }
    )
  }
}
