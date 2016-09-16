package controllers

import com.trifectalabs.osprey.v0.models.json._
import com.trifectalabs.osprey.v0.models.{UserActivityAttributes => AA, ActivityType, UserActivityAttributesForm}
import modules.DAOModule
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import java.util.UUID
import tables.UserActivityAttributesTable

class UserActivityAttributes extends Controller {
  def getByUserID(userID: UUID) = Action.async {
    UserActivityAttributesTable.findActiveByUserID(userID)
      .map{ resp => Ok(Json.toJson(resp))}
  }

  def patchByUserIDAndActivityType(userID: UUID, activityType: ActivityType) = Action.async(parse.json) { request =>
    request.body.validate[UserActivityAttributesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[UserActivityAttributesForm] => {
        val form = s.get
        UserActivityAttributesTable.findActiveByUserIDAndActivityType(userID, activityType)
          .map {_ match {
            case None => throw new RuntimeException("user has no attributes to update")
            case Some(ea) => 
              val newAttributes = AA(
                userID,
                activityType,
                level = form.level.getOrElse(ea.level),
                days = form.days.getOrElse(ea.days),
                activities = form.activities.getOrElse(ea.activities),
                longestDistance = form.longestDistance.getOrElse(ea.longestDistance),
                activityLengthRange = form.activityLengthRange.getOrElse(ea.activityLengthRange),
                variance = form.variance.getOrElse(ea.variance),
                activitySpecific = form.activitySpecific.getOrElse(ea.activitySpecific),
                new DateTime())
              UserActivityAttributesTable.remove(ea)
              UserActivityAttributesTable.add(newAttributes)
              Ok(Json.toJson(newAttributes))
          }
          }
      }
    }
  }

  def postByUserIDAndActivityType(userID: UUID, activityType: ActivityType) = Action.async(parse.json) { request =>
    request.body.validate[UserActivityAttributesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[UserActivityAttributesForm] => {
        val form = s.get
        val newAttributes = AA(
          userID,
          activityType,
          level = form.level.get,
          days = form.days.get,
          activities = form.activities.get,
          longestDistance = form.longestDistance.get,
          activityLengthRange = form.activityLengthRange.get,
          variance = form.variance.get,
          activitySpecific = form.activitySpecific.get,
          new DateTime())
        UserActivityAttributesTable.add(newAttributes)
        Future(Ok(Json.toJson(newAttributes)))
      }
    }
  }
}
