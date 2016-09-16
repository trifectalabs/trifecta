package models

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.trifectalabs.osprey.v0.models.json._
import com.trifectalabs.osprey.v0.models.{ActivityType, UserActivityAttributes}
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.functional.syntax._
import play.api.libs.json._

object json {
  implicit def jsonReadsRavenTrainingPlan:
  play.api.libs.json.Format[DisplayTrainingPlan] = {
    (
      (__ \ "id").format[Int] and
        (__ \ "userID").format[UUID] and
        (__ \ "activityType").format[ActivityType] and
        (__ \ "startDate").format[LocalDate] and
        (__ \ "endDate").format[LocalDate] and
        (__ \ "createdAt").format[DateTime] and
        (__ \ "archived").format[Boolean] and
        (__ \ "activities").format[List[DisplayTrainingPlanActivity]]
      ) (DisplayTrainingPlan.apply, unlift(DisplayTrainingPlan.unapply))
  }

  implicit def jsonReadsRavenTrainingPlanActivity:
  play.api.libs.json.Format[DisplayTrainingPlanActivity] = {
    (
      (__ \ "id").format[Int] and
        (__ \ "userDefined").format[Boolean] and
        (__ \ "distance").formatNullable[Double] and
        (__ \ "time").formatNullable[Double] and
        (__ \ "elevation").formatNullable[Double] and
        (__ \ "activityID").formatNullable[Int] and
        (__ \ "calendarEventID").formatNullable[String]
      )(DisplayTrainingPlanActivity.apply,
        unlift(DisplayTrainingPlanActivity.unapply))
  }

  implicit def jsonReadsUser: play.api.libs.json.Format[User] = {
    (
      (__ \ "id").format[UUID] and
      (__ \ "loginInfo").format[LoginInfo] and
      (__ \ "firstName").formatNullable[String] and
      (__ \ "lastName").formatNullable[String] and
      (__ \ "email").formatNullable[String] and
      (__ \ "city").formatNullable[String] and
      (__ \ "province").formatNullable[String] and
      (__ \ "country").formatNullable[String] and
      (__ \ "sex").formatNullable[String] and
      (__ \ "dateOfBirth").formatNullable[DateTime] and
      (__ \ "age").formatNullable[Int] and
      (__ \ "avatarURL").formatNullable[String] and
      (__ \ "height").formatNullable[Double] and
      (__ \ "weight").formatNullable[Double] and
      (__ \ "attributes").format[Map[String, UserActivityAttributes]]
    )(User.apply, unlift(User.unapply))
  }
}