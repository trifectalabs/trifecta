package controllers

import com.trifectalabs.osprey.v0.models.json._
import com.trifectalabs.osprey.v0.models.ActivityType
import modules.DAOModule
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import java.util.UUID
import tables.SingleActivitiesTable

class SingleActivities extends Controller {
  def getById(activityID: UUID) = Action.async {
    SingleActivitiesTable.findByID(activityID) map { activity =>
      Ok(Json.toJson(activity))
    }
  }

  def getUserByUserID(userID: UUID) = Action.async {
    SingleActivitiesTable.findAllByUserID(userID) map { activities =>
      Ok(Json.toJson(activities))
    }
  }
}
