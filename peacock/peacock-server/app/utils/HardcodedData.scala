package utils

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.trifectalabs.osprey.v0.models.{ActivityType, ActivityVariance, UserActivityAttributes}
import models._
import org.joda.time.{DateTime, LocalDate}

object HardcodedData {
  def defaultAttributes() : Map[String, UserActivityAttributes] = Map(
    "Ride" -> UserActivityAttributes(
      userID = 0,
      activityType = ActivityType.Ride,
      level = 1,
      days = 14,
      activities = 8,
      longestDistance = 50.0,
      variance = ActivityVariance(0.5, 0.5, 0),
      activitySpecific = Map(
        "Bike Rolling Resistance" -> 0.004,
        "Bike Drag" -> 1.0),
      createdAt = new DateTime(),
      archived = false),
    "Run" -> UserActivityAttributes(
      userID = 0,
      activityType = ActivityType.Run,
      level = 1,
      days = 14,
      activities = 8,
      longestDistance = 10.0,
      variance = ActivityVariance(0.5, 0.5, 0),
      activitySpecific = Map(),
      createdAt = new DateTime(),
      archived = false),
    "Swim" -> UserActivityAttributes(
      userID = 0,
      activityType = ActivityType.Swim,
      level = 1,
      days = 14,
      activities = 8,
      longestDistance = 1.0,
      variance = ActivityVariance(0.5, 0.5, 0),
      activitySpecific = Map(),
      createdAt = new DateTime(),
      archived = false))
  val defaultUser = User(
    id = UUID.randomUUID(),
    loginInfo = LoginInfo("", ""),
    firstName = None,
    lastName = None,
    email = None,
    city = None,
    province = None,
    country = None,
    sex = None,
    dateOfBirth = None,
    age = None,
    avatarURL = None,
    height = None,
    weight = None,
    attributes = Map())
  val plans = List(
    DisplayTrainingPlan(
      id = 1,
      userID = UUID.fromString("ac57a97c-4005-4735-8480-b8fda8ff4d0a"),
      activityType = ActivityType.Ride,
      startDate = new LocalDate(),
      endDate = new LocalDate(),
      createdAt = new DateTime(),
      archived = false,
      activities = List(
        DisplayTrainingPlanActivity(
          id = 1,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 2,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 3,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None))),
    DisplayTrainingPlan(
      id = 2,
      userID = UUID.fromString("ac57a97c-4005-4735-8480-b8fda8ff4d0a"),
      activityType = ActivityType.Run,
      startDate = new LocalDate(),
      endDate = new LocalDate(),
      createdAt = new DateTime(),
      archived = false,
      activities = List(
        DisplayTrainingPlanActivity(
          id = 1,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 2,
          userDefined = false,
          distance = Some(10.0),
          time = Some(60.0),
          elevation = Some(100.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 3,
          userDefined = false,
          distance = Some(10.0),
          time = Some(60.0),
          elevation = Some(100.0),
          activityID = None,
          calendarEventID = None))),
    DisplayTrainingPlan(
      id = 3,
      userID = UUID.fromString("ac57a97c-4005-4735-8480-b8fda8ff4d0a"),
      activityType = ActivityType.Ride,
      startDate = new LocalDate(),
      endDate = new LocalDate(),
      createdAt = new DateTime(),
      archived = false,
      activities = List(
        DisplayTrainingPlanActivity(
          id = 1,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 2,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None),
        DisplayTrainingPlanActivity(
          id = 3,
          userDefined = false,
          distance = Some(30.0),
          time = Some(60.0),
          elevation = Some(200.0),
          activityID = None,
          calendarEventID = None))))
}
