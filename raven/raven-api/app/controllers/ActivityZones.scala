package controllers

import play.api.mvc._
import play.api.libs.json.{JsSuccess, JsError, Json}
import services.{ActivityZonesService, FitnessPointsService}
import com.trifectalabs.osprey.v0.models.{SingleActivity, ActivityType}
import com.trifectalabs.raven.v0.models.ZoneType
import com.trifectalabs.raven.v0.models.json._
import com.trifectalabs.osprey.v0.models.json._
import modules.{OspreyModule, DAOModule}
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import tables.ActivityZonesTable

class ActivityZones extends Controller with OspreyModule {
  lazy val service = new ActivityZonesService()
  lazy val fitnessPointsService = new FitnessPointsService()

  def getByActivityID(activityID: UUID) = Action.async {
    ActivityZonesTable.findByID(activityID) map { zones =>
     Ok(Json.toJson(zones))
    }
  }

  def getByActivityIDAndZoneType(
    activityID: UUID, 
    zoneType: ZoneType
  ) = Action.async {
    ActivityZonesTable.findByID(activityID).map{ allZones =>
      allZones.exists(_.zoneType == zoneType) match {
        case true => Ok(Json.toJson(allZones.filter(_.zoneType == zoneType).head))
        case false => NotFound
      }
    }
  }

  def post() = Action.async(parse.json) { request =>
    request.body.validate[SingleActivity] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[SingleActivity] =>
        val activity = s.get
        println(s"Processing activity zones for activity ${activity.id}")
        for {
          zones <- service.getZonesFromActivity(activity)
          a <- service.saveAllZonesForActivity(zones)
        } yield {
          Ok("")
        }
    }
  }

  def postLevelByUserIDAndActivityType(
    userID: UUID, 
    activityType: ActivityType
  ) = Action.async(parse.json) { request =>
    request.body.validate[List[SingleActivity]] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[List[SingleActivity]] =>
        val activities = activityType match {
          case ActivityType.Ride =>
            for {
              r <- activitiesClient.getUserByUserID(userID) map { allActs =>
                allActs.filter(_.activityType == ActivityType.Ride)
              }
                
              v <- activitiesClient.getUserByUserID(userID) map { allActs =>
                allActs.filter(_.activityType == ActivityType.VirtualRide)
              }
            } yield {
              r ++ v
            }
          case _ =>
            activitiesClient.getUserByUserID(userID) map { allActs =>
              allActs.filter(_.activityType == activityType)
            }
        }
        activities flatMap { a =>
          fitnessPointsService.getInitLevelOfUser(a.toList) map { level =>
            println(s"Activity Type: ${activityType}, Level: ${level}")
            Ok(Json.toJson(level))
          }
        }
    }
  }
}

