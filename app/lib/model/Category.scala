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

  sealed abstract class CategoryColor(
    val code: Short,
    val name: String,
    val color: String
  ) extends EnumStatus
  object CategoryColor extends EnumStatus.Of[CategoryColor] {
    case object COLOR_OPTION1 extends CategoryColor(code = 1, name = "赤", color = "#FF0000")
    case object COLOR_OPTION2 extends CategoryColor(code = 2, name = "緑", color = "#00FF00")
    case object COLOR_OPTION3 extends CategoryColor(code = 3, name = "青", color = "#0000FF")
    case object COLOR_OPTION4 extends CategoryColor(code = 4, name = "黄色", color = "#FFFF00")
    case object COLOR_OPTION5 extends CategoryColor(code = 5, name = "マゼンタ", color = "#FF00FF")
    case object COLOR_OPTION6 extends CategoryColor(code = 6, name = "シアン", color = "#00FFFF")
  }

  def apply(
    name: String,
    slug: String,
    categoryColor: CategoryColor
  ): Category#WithNoId =
    new Category(
      id = None,
      name = name,
      slug = slug,
      categoryColor = categoryColor,
    ).toWithNoId
}
