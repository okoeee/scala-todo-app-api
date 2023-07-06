package json.writes

import ixias.util.json.JsonEnvWrites
import lib.model.Category
import model.ViewValueCategory
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Writes}

case class JsValueCategoryListItem(
  id: Category.Id,
  name: String,
  slug: String,
  categoryColor: Category.CategoryColor
)

object JsValueCategoryListItem extends JsonEnvWrites {
  implicit val writes: Writes[JsValueCategoryListItem] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "slug").write[String] and
      (JsPath \ "categoryColor")
        .write(EnumStatusWrites)
  )(unlift(JsValueCategoryListItem.unapply))

  def apply(viewValueCategory: ViewValueCategory): JsValueCategoryListItem = {
    JsValueCategoryListItem(
      id = viewValueCategory.id,
      name = viewValueCategory.name,
      slug = viewValueCategory.slug,
      categoryColor = viewValueCategory.colorCategory
    )
  }
}
