package controllers

import play.api.mvc._
import play.api.libs.json._
import com.trifectalabs.osprey.v0.models.{UserPhysicalAttributesForm, UserPhysicalAttributes => PA}
import com.trifectalabs.osprey.v0.models.json._
import org.joda.time.DateTime
import modules.DAOModule
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import tables.UserPhysicalAttributesTable

class UserPhysicalAttributes extends Controller {
  def getByUserID(userID: UUID) = Action.async {
    UserPhysicalAttributesTable.findActiveByUserID(userID) map { data =>
      Ok(Json.toJson(data))
    }
  }

  def patchByUserID(userID: UUID) = Action.async(parse.json) { request =>
    request.body.validate[UserPhysicalAttributesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[UserPhysicalAttributesForm] => {
        val form = s.get
          UserPhysicalAttributesTable.findActiveByUserID(userID) map { _ match {
            case None => throw new RuntimeException("user has no attributes to update")
            case Some(ea) => 
              val newAttributes = PA(
                userID,
                form.height.getOrElse(ea.height),
                form.weight.getOrElse(ea.weight),
                form.waist,
                form.bmi)
              UserPhysicalAttributesTable.remove(ea)
              UserPhysicalAttributesTable.add(newAttributes)
              Ok(Json.toJson(newAttributes))
          }
          }
      }
    }
  }

  def postByUserID(userID: UUID) = Action.async(parse.json) { request =>
    request.body.validate[UserPhysicalAttributesForm] match {
      case e: JsError => Future(Conflict(e.toString))
      case s: JsSuccess[UserPhysicalAttributesForm] => {
        val form = s.get
        val newAttributes = PA(
          userID,
          form.height.get,
          form.weight.get,
          form.waist,
          form.bmi)
        UserPhysicalAttributesTable.add(newAttributes)
        Future(Ok(Json.toJson(newAttributes)))
      }
    }
  }
}
