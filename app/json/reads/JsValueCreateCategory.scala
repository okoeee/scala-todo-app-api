package json.reads

import ixias.util.json.JsonEnvReads
import lib.model.Category
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class JsValueCreateCategory(
  name: String,
  slug: String,
  categoryColorId: Category.CategoryColor
)

object JsValueCreateCategory extends JsonEnvReads {
  implicit val reads: Reads[JsValueCreateCategory] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "slug").read[String] and
      (JsPath \ "categoryColorId")
        .read[Category.CategoryColor](enumReads(Category.CategoryColor))
  )(JsValueCreateCategory.apply _)
}
