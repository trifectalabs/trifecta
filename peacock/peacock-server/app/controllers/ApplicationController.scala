package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import models.json._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, SessionAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, SessionAuthenticator] {

  /**
    * Handles the dashboard action.
    *
    * @return The result to display.
    */
  def init = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future(Redirect(routes.ApplicationController.dashboard()))
      case None => Future(Ok(views.html.index("Trifecta")))
    }
  }

  /**
   * Handles the dashboard action.
   *
   * @return The result to display.
   */
  def dashboard = SecuredAction.async { implicit request =>
    Future(Ok(views.html.index("Trifecta")))
  }

  /**
    * Handles the onboarding action.
    *
    * @return The result to display.
    */
  def onboarding = SecuredAction.async { implicit request =>
    Future(Ok(views.html.index("Trifecta")))
  }

  /**
    * Handles the profile action.
    *
    * @return The result to display.
    */
  def profile = SecuredAction.async { implicit request =>
    Future(Ok(views.html.index("Trifecta")))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future(Redirect(routes.ApplicationController.dashboard()))
      case None => Future(Ok(views.html.index("Trifecta")))
    }
  }

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signUp = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future(Redirect(routes.ApplicationController.dashboard()))
      case None => Future(Ok(views.html.index("Trifecta")))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, Ok)
  }
}
