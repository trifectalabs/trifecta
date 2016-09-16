package controllers

import modules.DAOModule
import org.joda.time.DateTime
import play.api.libs.json.{JsSuccess, JsError, Json}
import play.api.mvc.{Action, Controller}
import com.trifectalabs.raven.v0.models.{TrainingZonesForm, ZoneType,
  TrainingZones => TZ}
import com.trifectalabs.raven.v0.models.json._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import tables._

class TrainingZones extends Controller {
  def getByUserID(userID: UUID) = Action.async {
    TrainingZonesTable.findByUserID(userID) map { zones =>
      Ok(Json.toJson(zones))
    }
  }

  def getByUserIDAndZoneType(userID: UUID, zoneType: ZoneType) = Action.async {
    TrainingZonesTable.findByUserID(userID).map { allZones =>
      val zones = allZones.filter(_.zoneType == zoneType) 
      Ok(Json.toJson(zones))
    }
  }

  def putByUserIDAndZoneType(
    userID: UUID, 
    zoneType: ZoneType
  ) = Action.async(parse.json) { request =>
    request.body.validate[TrainingZonesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[TrainingZonesForm] =>
        val form = s.get
        TrainingZonesTable.findByUserID(userID).map { allExistingZones =>
          val existingZones = allExistingZones.filter(_.zoneType == zoneType).head
          val newZones = TZ(
            userID = userID,
            zoneType = zoneType,
            zoneOne = form.zoneOne.getOrElse(existingZones.zoneOne),
            zoneTwo = form.zoneTwo.getOrElse(existingZones.zoneTwo),
            zoneThree = form.zoneThree.getOrElse(existingZones.zoneThree),
            zoneFour = form.zoneFour.getOrElse(existingZones.zoneFour),
            zoneFive = form.zoneFive.getOrElse(existingZones.zoneFive),
            createdAt = new DateTime())
          TrainingZonesTable.remove(existingZones)
          TrainingZonesTable.add(newZones)
          Ok(Json.toJson(newZones))
        }
    }
  }

  def postByUserIDAndZoneType(
    userID: UUID, 
    zoneType: ZoneType
  ) = Action.async(parse.json) { request =>
    request.body.validate[TrainingZonesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[TrainingZonesForm] =>
        val form = s.get
        val zones = TZ(
          userID = userID,
          zoneType = zoneType,
          zoneOne = form.zoneOne.get,
          zoneTwo = form.zoneTwo.get,
          zoneThree = form.zoneThree.get,
          zoneFour = form.zoneFour.get,
          zoneFive = form.zoneFive.get,
          createdAt = new DateTime())
        TrainingZonesTable.add(zones)
        Future(Ok(Json.toJson(zones)))
    }
  }
}
