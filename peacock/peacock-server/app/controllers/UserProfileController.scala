package controllers

import java.util.NoSuchElementException
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import models.json._
import models.services.UserService
import play.api.i18n.MessagesApi
import play.api.libs.json.{Json, JsError, JsSuccess}
import utils.HardcodedData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * The controller for communicating with Raven.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param socialProviderRegistry The social provider registry.
  */
class UserProfileController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[User, SessionAuthenticator],
  userService: UserService,
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, SessionAuthenticator] {

  /**
    * Handles the user post action.
    */
  def postUser = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[User] match {
      case e: JsError => println(e.toString); Future(BadRequest(e.toString))
      case s: JsSuccess[User] =>
        val user = s.get
        try {
          for {
            oldUser <- userService.retrieve(user.loginInfo).map(_.get)
            newUser <- userService.save(user)
              .fallbackTo(Future.successful(oldUser))
          } yield {
            if (newUser.equals(oldUser)) {
              InternalServerError(Json.toJson(newUser))
            } else {
              Ok(Json.toJson(newUser))
            }
          }
        } catch {
          case e: NoSuchElementException =>
            println(e.toString) // TODO: logging
            Future(InternalServerError)
        }
    }
  }
}
