package models

import java.util.UUID

import com.trifectalabs.osprey.v0.models.ActivityType
import org.joda.time.{DateTime, LocalDate}

case class DisplayTrainingPlan(
  id: Int,
  userID: UUID,
  activityType: ActivityType,
  startDate: LocalDate,
  endDate: LocalDate,
  createdAt: DateTime,
  archived: Boolean,
  activities: List[DisplayTrainingPlanActivity])

case class DisplayTrainingPlanActivity(
  id: Int,
  userDefined: Boolean,
  distance: Option[Double] = None,
  time: Option[Double] = None,
  elevation: Option[Double] = None,
  activityID: Option[Int] = None,
  calendarEventID: Option[String] = None)
