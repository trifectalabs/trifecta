package controllers

import play.api.mvc._
import play.api.libs.json._
import services.SchedulingService
import java.util.UUID
import org.joda.time.DateTime
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.trifectalabs.raven.v0.models.json._
import com.trifectalabs.social.weaver.v0.models.json._
import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import com.trifectalabs.osprey.v0.models.ActivityType
import modules.RavenModule

class TrainingSchedules extends Controller with RavenModule {
  lazy val service = new SchedulingService()

  def postScheduleByUserID(
    userID: UUID,
    activityType: ActivityType,
    startTime: DateTime,
    numberOfSchedulesToBeCreated: Option[Int]
  ) = Action.async(parse.json) { request =>
    request.body.validate[List[TrainingPlanActivity]] match {
      case e: JsError => Future(BadRequest(e.toString))
      case s: JsSuccess[List[TrainingPlanActivity]] =>
        val plan = s.get
        service.createScheduleForTrainingPlan(
          userID,
          activityType,
          startTime,
          plan).map(s => Ok(Json.toJson(List(s))))
    }
  }
}
