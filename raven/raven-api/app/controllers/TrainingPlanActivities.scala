package controllers

import play.api.mvc._
import play.api.libs.json._
import com.trifectalabs.osprey.v0.models.ActivityType
import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import com.trifectalabs.raven.v0.models.json._
import services.trainingplan.{TrainingPlanUtil, TrainingPlanService}
import modules.{OspreyModule, DAOModule}
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import tables._

class TrainingPlanActivities extends Controller 
  with OspreyModule 
{
  lazy val service = new TrainingPlanService()

  def getById(id: UUID) = Action.async {
    TrainingPlanActivitiesTable.findByID(id) map { optAct =>
      optAct match {
        case Some(act) => Ok(Json.toJson(act))
        case None => NotFound
      }
    }
  }

  def getUserByUserID(userID: UUID, offset: Int, limit: Int) = Action.async {
    TrainingPlanActivitiesTable.findByUserID(userID) map { allActs =>
      Ok(Json.toJson(allActs))
    }
  }

  def deleteById(id: UUID) = Action.async {
    for {
      a <- TrainingPlanActivitiesTable.remove(id) 
    } yield {
      Ok("")
    }
  }

  def patch() = Action.async(parse.json) { request =>
    request.body.validate[TrainingPlanActivity] match {
      case e: JsError => Future(BadRequest(e.toString))
      case s: JsSuccess[TrainingPlanActivity] =>
        val tp = s.get
        TrainingPlanActivitiesTable.amend(tp)
        Future(Ok(""))
    }
  }

  def postGenerateByUserID(
    userID: UUID, 
    activityType: ActivityType
  ) = Action.async(parse.json) { request =>
    request.body.validate[List[TrainingPlanActivity]] match {
      case e: JsError => Future(BadRequest(e.toString))
      case s: JsSuccess[List[TrainingPlanActivity]] =>
        val partiallyDefinedActivities = s.get
        (for {
          pa <- userPhysicalAttributesClient.getByUserID(userID)
          aa <- userActivityAttributesClient.getByUserID(userID)
          userTraits = TrainingPlanUtil.buildUserTraits(pa, aa)
          trainingPlanActivities <- service.buildTrainingPlan(
            userTraits, 
            activityType,
            // TODO: gather uncompleted scheduled activities
            // in order to generate continuous training plan
            List(),
            partiallyDefinedActivities)
        } yield {
          trainingPlanActivities.foreach(a => 
            TrainingPlanActivitiesTable.add(a))
          Ok(Json.toJson(trainingPlanActivities))
        }) recover {
          // TODO: add logging
          case e => 
            println(e)
            InternalServerError
        }
    }
  }
}
