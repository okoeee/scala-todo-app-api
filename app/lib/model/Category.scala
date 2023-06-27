package lib.model

import ixias.model._
import ixias.util.EnumStatus
import lib.model.Category.CategoryColor

import java.time.LocalDateTime

case class Category(
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

  sealed abstract class CategoryColor(val code: Short,
                                      val name: String,
                                      val color: String)
    extends EnumStatus
  object CategoryColor extends EnumStatus.Of[CategoryColor] {
    case object IS_FRONTEND
      extends CategoryColor(code = 1, name = "フロントエンド", color = "#FF5722")
    case object IS_BACKEND
      extends CategoryColor(code = 2, name = "バックエンド", color = "#4CAF50")
    case object IS_INFRA
      extends CategoryColor(code = 3, name = "インフラ", color = "#03A9F4")
  }

  def apply(name: String,
            slug: String,
            categoryColor: CategoryColor): Category#WithNoId =
    new Category(
      id = None,
      name = name,
      slug = slug,
      categoryColor = categoryColor,
    ).toWithNoId
}
