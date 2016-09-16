package controllers

import play.api.libs.json.Json
import play.api.mvc._
import services._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import com.trifectalabs.arctic.tern.v0.models.json._

object Routes extends Controller {
	val routeService = new RouteService()

	def get(distance: Double, elevation: Double, start_lat: Double, start_lng: Double) = Action.async {
		val route = routeService.generateRoute(distance, start_lat, start_lng, elevation)
		Future(Ok(Json.toJson(route)))
	}
}
