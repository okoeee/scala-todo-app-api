package service

import json.reads.JsValueCreateCategory
import lib.model.Category
import lib.model.Category.CategoryColor
import lib.persistence.onMySQL.CategoryRepository
import model.ViewValueCategory
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CategoryService {

  private val logger = Logger(this.getClass)

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
      categoryColor = CategoryColor(code = categoryFormData.categoryColorId)
    )
    CategoryRepository.add(categoryWithNoId).map { _ =>
      logger.info("カテゴリを作成しました")
      Right("カテゴリを作成しました")
    } recover {
      case e: Exception =>
        logger.error("カテゴリの作成に失敗しました", e)
        Left("カテゴリの作成に失敗しました")
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
            categoryColor =
              CategoryColor(code = categoryFormData.categoryColorId)
          )
        )
        CategoryRepository.update(updatedCategory).map { _ =>
          logger.info("カテゴリを更新しました")
          Right("カテゴリを更新しました")
        } recover {
          case e: Exception =>
            logger.error("カテゴリの更新に失敗しました", e)
            Left("カテゴリの更新に失敗しました")
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
                logger.info("カテゴリを削除しました")
                Right("カテゴリを削除しました")
            }
        } recover {
          case e: Exception =>
            logger.info("カテゴリの削除に失敗しました", e)
            Left("カテゴリの削除に失敗しました")
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
