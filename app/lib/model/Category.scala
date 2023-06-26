package lib.model

import ixias.model._
import ixias.util.EnumStatus
import lib.model.Category.CategoryColor
import lib.model.Category.CategoryColor.IS_FRONTEND

import java.time.LocalDateTime

case class Category (
  id: Option[Category.Id],
  name: String,
  slug: String,
  categoryColor: CategoryColor,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Category.Id]

object Category {
  val Id = the[Identity[Id]]
  type Id = Long @@ Category
  type WithNoId = Entity.WithNoId[Id, Category]
  type EmbeddedId = Entity.EmbeddedId[Id, Category]

  sealed abstract class CategoryColor(val code: Short, val color: String) extends EnumStatus
  object CategoryColor extends EnumStatus.Of[CategoryColor] {
    case object IS_FRONTEND extends CategoryColor(code = 1, color = "#FF5722") // フロントエンド
    case object IS_BACKEND extends CategoryColor(code = 2, color = "#4CAF50") // バックエンド
    case object IS_INFRA extends CategoryColor(code = 3, color = "#03A9F4") // インフラ
  }

  def apply(name: String, slug: String, categoryColor: CategoryColor): Category#WithNoId =
    new Category(
      id = None,
      name = name,
      slug = slug,
      categoryColor = categoryColor,
    ).toWithNoId
}