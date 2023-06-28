package model

import lib.model.Category.CategoryColor

case class ViewValueCategory (
  id: Long,
  name: String,
  slug: String,
  colorCategory: CategoryColor
)
