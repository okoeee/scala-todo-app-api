package service

import forms.{LoginForm, RegistrationForm}
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

  def login(loginForm: LoginForm): Future[Either[String, User.Id]] = {
    UserRepository.getByEmail(loginForm.email).flatMap {
      case None => Future.successful(Left("メールアドレスかパスワードが間違っています"))
      case Some(user) if user.v.password == loginForm.password =>
        Future.successful(Right(user.id))
    }
  }

}
