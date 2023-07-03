package service

import json.reads.JsValueCreateCategory
import lib.model.Category
import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.CategoryRepository
import model.ViewValueCategory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CategoryService {

  def get(
    categoryId: Category.Id
  ): Future[Either[String, ViewValueCategory]] = {
    CategoryRepository.get(categoryId).map {
      case None => Left("カテゴリが見つかりませんでした")
      case Some(embeddedCategory) =>
        Right(
          ViewValueCategory(
            id = embeddedCategory.id,
            name = embeddedCategory.v.name,
            slug = embeddedCategory.v.slug,
            colorCategory = embeddedCategory.v.categoryColor
          )
        )
    }
  }
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

  def update(
    categoryId: Category.Id,
    categoryFormData: JsValueCreateCategory
  ): Future[Either[String, String]] = {
    CategoryRepository.get(categoryId).flatMap {
      case None => Future.successful(Left("カテゴリが見つかりませんでした"))
      case Some(embeddedCategory) =>
        val updatedCategory = embeddedCategory.map(
          _.copy(
            name = categoryFormData.name,
            slug = categoryFormData.slug,
            categoryColor = CategoryColor(code = categoryFormData.categoryId)
          )
        )
        CategoryRepository.update(updatedCategory).map { _ =>
          Right("カテゴリを更新しました")
        } recover {
          case _: Exception => Left("カテゴリの更新に失敗しました")
        }
    }
  }

  def remove(categoryId: Category.Id): Future[Either[String, String]] = {
    CategoryRepository.get(categoryId).flatMap {
      case None => Future.successful(Left("削除対象のカテゴリが見つかりませんでした"))
      case Some(embeddedCategory) =>
        CategoryRepository.remove(embeddedCategory.id).flatMap {
          case None => Future.successful(Left("カテゴリの削除に失敗しました"))
          case Some(embeddedCategory) =>
            TodoService.updateTodosOfNoneCategory(embeddedCategory.id).map {
              _ =>
                Right("カテゴリを削除しました")
            }
        } recover {
          case _: Exception => Left("カテゴリの削除に失敗しました")
        }
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
