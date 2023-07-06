package model

import lib.model.Category
import lib.model.Category.CategoryColor

case class ViewValueCategory(
  id: Category.Id,
  name: String,
  slug: String,
  colorCategory: CategoryColor
)
