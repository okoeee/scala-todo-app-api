package service

import lib.persistence.onMySQL.CategoryRepository

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

}
