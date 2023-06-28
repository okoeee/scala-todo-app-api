package service

import lib.persistence.onMySQL.CategoryRepository
import model.ViewValueCategory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CategoryService {

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
          name = category.v.name,
          slug = category.v.slug,
          colorCategory = category.v.categoryColor
        )
      }
    }
  }

}
