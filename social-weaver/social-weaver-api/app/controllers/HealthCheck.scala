package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthCheck() extends Controller {
  def get() = Action.async {
    Future(Ok(Json.toJson("healthy")))
  }
}
