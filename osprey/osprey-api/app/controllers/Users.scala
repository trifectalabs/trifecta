package controllers

import com.trifectalabs.osprey.v0.models.{User, UserForm}
import com.trifectalabs.osprey.v0.models.json._
import com.trifectalabs.osprey.v0.models.ActivityType
import org.joda.time.DateTime
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import kiambogo.scrava._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import java.util.UUID
import modules.DAOModule
import tables.UserTable
import services.StravaAuthService

class Users extends Controller with DAOModule {
  def post() = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm] match {
      case e: JsError => Future(BadRequest(e.toString))
      case jsForm: JsSuccess[UserForm] => {
        val userForm = jsForm.get
        val id = UUID.randomUUID()
        val user = User(
          id = id,
          firstName = userForm.firstName,
          lastName = userForm.lastName,
          email = userForm.email,
          city = userForm.city,
          province = userForm.province,
          country = userForm.country,
          sex = userForm.sex,
          dateOfBirth = userForm.dateOfBirth,
          age = userForm.age,
          avatarURL = userForm.avatarURL,
          stravaToken = userForm.stravaToken,
          stravaAvatar = userForm.stravaAvatar,
          privacy = userForm.privacy,
          role = userForm.role,
          createdAt = DateTime.now())
        UserTable.add(user)
        user.stravaToken match {
          case Some(token) => {
            val strava = new StravaAuthService(new ScravaClient(token))
            Future(strava.synchronizeNewAthleteActivities(id))
          }
          case None =>
            // do nothing
        }
        Future(Ok(Json.toJson(id)))
      }
    }
  }

  def patch(userID: UUID) = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm] match {
      case e: JsError => { println(e); Future(BadRequest(e.toString)) }
      case jsForm: JsSuccess[UserForm] => {
        val form = jsForm.get
        UserTable.findByID(userID) flatMap { _ match {
          case Some(existingUser) => {
            val modifiedUser = existingUser.copy(
              firstName = form.firstName,
              lastName = form.lastName,
              email = form.email,
              city = form.city,
              province = form.province,
              country = form.country,
              sex = form.sex,
              dateOfBirth = form.dateOfBirth,
              age = form.age,
              avatarURL = form.avatarURL,
              stravaToken = form.stravaToken,
              stravaAvatar = form.stravaAvatar,
              googleCalendarAccessToken = form.googleCalendarAccessToken,
              googleCalendarRefreshToken = form.googleCalendarRefreshToken,
              googleCalendarIDs = form.googleCalendarIDs,
              privacy = form.privacy,
              role = form.role)
            UserTable.amend(modifiedUser)
            Future(Ok(Json.toJson(modifiedUser)))
          }
          case None => Future(BadRequest("Invalid user ID"))
        }
        }
      }
    }
  }

  def getAllUserIDs = Action.async {
    UserTable.getAllIDs map { ids =>
      Ok(Json.toJson(ids))
    }
  }

  def getById(userID: UUID) = Action.async {
    UserTable.findByID(userID).map {a => a match {
      case Some(u) => Ok(Json.toJson(u))
      case None => NotFound("User not found")
    } }
  }

  def getEmailByEmail(email: String) = Action.async {
    UserTable.findByEmail(email) map { _ match {
      case Some(u) => Ok(Json.toJson(u))
      case None => NotFound("User not found")
    }
    }
  }
  
  def getExistsByEmail(email: String) = Action.async {
    UserTable.findByEmail(email) map { _ match {
      case Some(u) => Ok(Json.toJson(true))
      case None => Ok(Json.toJson(false))
    }
    }
  }
}

