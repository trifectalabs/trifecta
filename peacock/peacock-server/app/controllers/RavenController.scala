package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import models.json._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import utils.HardcodedData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * The controller for communicating with Raven.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param socialProviderRegistry The social provider registry.
  */
class RavenController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, SessionAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, SessionAuthenticator] {

  /**
    * Handles the trainingPlans action.
    *
    * @return List of training plans
    */
  def trainingPlans(userID: String, offset: String, limit: String) =
    SecuredAction.async { implicit request =>
      (for {
        id <- Try(UUID.fromString(userID))
        off <- Try(offset.toInt)
        lim <- Try(limit.toInt)
      } yield {
        (id, lim, off)
      }) match {
        case Success((id, lim, off)) =>
          // TODO: fetch training plans from Raven
          val plans = HardcodedData.plans//.filter(_.userID == id)
          plans match {
            case Nil => Future(NotFound)
            case _ => Future(Ok(Json.toJson(plans)))
          }
        case Failure(ex) => Future(BadRequest)
      }
  }

  /**
    * Handles the trainingPlan action.
    *
    * @return List of training plans
    */
  def trainingPlan(trainingPlanID: String) =
    SecuredAction.async { implicit request =>
      Try(trainingPlanID.toInt) match {
        case Success(id) =>
          // TODO: fetch training plans from Raven
          val plans = HardcodedData.plans.filter(_.id == id)
          plans match {
            case Nil => Future(NotFound)
            case head::_ => Future(Ok(Json.toJson(head)))
          }
        case Failure(ex) => Future(BadRequest)
      }
    }
}
