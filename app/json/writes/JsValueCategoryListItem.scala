package json.writes

import model.ViewValueCategory
import play.api.libs.json.{Json, Writes}

case class JsValueCategoryListItem(
  id: Long,
  name: String,
  slug: String,
  categoryColor: Short
)

object JsValueCategoryListItem {
  implicit val writes: Writes[JsValueCategoryListItem] =
    Json.writes[JsValueCategoryListItem]

  def apply(viewValueCategory: ViewValueCategory): JsValueCategoryListItem = {
    JsValueCategoryListItem(
      id = viewValueCategory.id,
      name = viewValueCategory.name,
      slug = viewValueCategory.slug,
      categoryColor = viewValueCategory.colorCategory.code
    )
  }
}
