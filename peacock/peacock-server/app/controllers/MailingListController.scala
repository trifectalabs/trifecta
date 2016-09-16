package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import models.json._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import scala.util.parsing.json.JSONObject
import scalaj.http.Http
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import modules.ConfigModule 

/**
 * The mailing list controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
class MailingListController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, SessionAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, SessionAuthenticator]
  with ConfigModule {

  /**
   * Handles the Mailing List Sign Up action.
   *
   * @return The result to display.
   */
  def add(email: String) = UserAwareAction.async { implicit request =>
    val mailchimpApiKey = config.getString("mailchimp.apiKey")
    val mailchimpUrl = config.getString("mailchimp.url")
    val mailchimpListID = config.getString("mailchimp.listID")
    val data = Map(
      "status" -> "subscribed",
      "email_address" -> email)
    val response = 
      Http(s"${mailchimpUrl}/lists/${mailchimpListID}/members")
        .method("POST")
        .header("Authorization", s"Basic ${mailchimpApiKey}")
        .header("Content-Type", "application/json")
        .postData(JSONObject(data).toString())
        .asString
    response.code match {
      case 200 => Future(NoContent)
      case _ => Future(Conflict)
    }
  }
}

