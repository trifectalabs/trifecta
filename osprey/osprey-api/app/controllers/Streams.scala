package controllers

import com.trifectalabs.osprey.v0.models._
import com.trifectalabs.osprey.v0.models.json._
import modules.DAOModule
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import java.util.UUID
import tables.StreamTable

class Streams extends Controller {
  def get(activityID: UUID, streamTypes: Seq[StreamType]) = Action.async { request =>
    val s: Future[Seq[Stream]] = streamTypes match {
      case Nil => StreamTable.findByActivityID(activityID)
      case _ => StreamTable.findByActivityID(activityID, streamTypes)
    }
    s map { r =>
      Ok(Json.toJson(r))
    }
  }
}
