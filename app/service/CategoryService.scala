package service

import json.reads.JsValueCreateCategory
import lib.model.Category
import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.CategoryRepository
import model.ViewValueCategory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CategoryService {

  def add(
    categoryFormData: JsValueCreateCategory): Future[Either[String, String]] = {
    val categoryWithNoId = Category(
      name = categoryFormData.name,
      slug = categoryFormData.slug,
      categoryColor = CategoryColor(code = categoryFormData.categoryId)
    )
    CategoryRepository.add(categoryWithNoId).map { _ =>
      Right("カテゴリを作成しました")
    } recover {
      case _: Exception => Left("カテゴリの作成に失敗しました")
    }
  }

  /**
    * Formのselectボックスで使用する値を返す
    */
  def getOptionsOfCategory: Future[Seq[(String, String)]] = {
    CategoryRepository.getAll.map { seqCategories =>
      seqCategories.map { category =>
        (category.id.toString, category.v.name)
      }
    }
  }

  def getViewValueCategory: Future[Seq[ViewValueCategory]] = {
    CategoryRepository.getAll.map { seqCategory =>
      seqCategory.map { category =>
        ViewValueCategory(
          id = category.id,
          name = category.v.name,
          slug = category.v.slug,
          colorCategory = category.v.categoryColor
        )
      }
    }
  }

}
