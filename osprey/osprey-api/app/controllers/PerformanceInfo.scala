package controllers

import com.trifectalabs.osprey.v0.models.json._
import com.trifectalabs.osprey.v0.models._
import modules.DAOModule
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import java.util.UUID
import tables.PerformanceInfoTable

class PerformanceInfo extends Controller {
  def post() = Action.async(parse.json) { request =>
    request.body.validate[PerformanceInfoForm] match {
      case e: JsError => Future(Conflict(""))
      case jsForm: JsSuccess[PerformanceInfoForm] => {
        val form = jsForm.get
        val id = UUID.randomUUID()
        val perfInfo = PerformanceInfo(
          id = id,
          distance = form.distance,
          movingTime = form.movingTime,
          elapsedTime = form.elapsedTime,
          totalElevationGain = form.totalElevationGain,
          averageSpeed = form.averageSpeed,
          maxSpeed = form.maxSpeed,
          averagePower = form.averagePower,
          averageCadence = form.averageCadence,
          averageTemp = form.averageTemp,
          kilojoules = form.kilojoules,
          averageHeartrate = form.averageHeartrate,
          maxHeartrate = form.maxHeartrate,
          calories = form.calories)
        PerformanceInfoTable.add(perfInfo) map { _ =>
          Ok(Json.toJson(id))
        }
      }
    }
  }

  def getById(performanceInfoID: UUID) = Action.async {
    PerformanceInfoTable.findByID(performanceInfoID) map { data =>
      Ok(Json.toJson(data))
    }
  }
}
