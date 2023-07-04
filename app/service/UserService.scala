package service

import forms.RegistrationForm
import lib.model.User
import lib.persistence.onMySQL.UserRepository
import mvc.auth.UserAuthProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserService {

  def registration(
    registrationForm: RegistrationForm,
  ): Future[Either[String, User.Id]] = {
    UserRepository.getByEmail(registrationForm.email).flatMap {
      case Some(_) => Future.successful(Left("既に登録されているメールアドレスです"))
      case None => {
        val userWithNoId = User(
          name = registrationForm.name,
          email = registrationForm.email,
          password = registrationForm.password
        )
        UserRepository.add(userWithNoId).map { userId =>
          Right(userId)
        } recover {
          case _: Exception => Left("登録に失敗しました")
        }
      }
    }
  }

}
