package lib.persistence

import ixias.persistence.SlickRepository
import lib.model.{Category, Todo}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class TodoRepository[P <: JdbcProfile]()(implicit val driver: P)
  extends SlickRepository[Todo.Id, Todo, P]
  with db.SlickResourceProvider[P] {

  import api._

  /**
    * Get Todo Data
    */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") {
      _.filter(_.id === id).result.headOption
    }

  /**
    * Get All Todo Data
    */
  def getAll: Future[Seq[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") { _.sortBy(_.id).result }

  /**
    * Get All Todo filtered by categoryId
    */
  def getAllFilteredByCategoryId(
    categoryId: Category.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") {
      _.filter(_.categoryId === categoryId).result
    }

  /**
    * Add Todo Data
    */
  def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(TodoTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  /**
    * Update Todo Data
    */
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable) { slick =>
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
    * Bulk update Todo Data
    */
  def bulkUpdate(entities: Seq[EntityEmbeddedId]): Future[Int] = {
    val result: Future[Seq[Int]] =
      RunDBAction(TodoTable) { slick =>
        {
          DBIO.sequence(
            entities.map { d =>
              slick.filter(_.id === d.v.id).update(d.v)
            }
          )
        }.transactionally
      }
    result.map(_.sum)
  }

  /**
    * Delete Todo Data
    */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable) { slick =>
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
