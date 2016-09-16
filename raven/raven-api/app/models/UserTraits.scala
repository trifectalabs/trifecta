package models

import com.trifectalabs.osprey.v0.models.{ActivityVariance, ActivityLengthRange}
import java.util.UUID

case class UserTraits (
  userID: UUID,
  height: Double,
  weight: Double,
  runningDays: Int,
  cyclingDays: Int,
  runningActivities: Int,
  cyclingActivities: Int,
  cyclingLevel: Double,
  runningLevel: Double,
  cyclingVariance: ActivityVariance,
  runningVariance: ActivityVariance,
  cyclingActivityLengthRange: Map[String, ActivityLengthRange],
  runningActivityLengthRange: Map[String, ActivityLengthRange],
  bikeRollingResistance: Double,
  bikeDrag: Double,
  furthestDistanceBike: Double,
  furthestDistanceRun: Double)
