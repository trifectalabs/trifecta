package services.trainingplan

import com.trifectalabs.osprey.v0.models.{UserActivityAttributes, 
  UserPhysicalAttributes}
import models.UserTraits
import services.trainingplan.functions._
import com.trifectalabs.osprey.v0.models.{ActivityVariance, ActivityType}
import com.trifectalabs.raven.v0.models.{ActivityLength, TrainingPlanActivity}
import org.joda.time.DateTime
import java.util.UUID

object TrainingPlanUtil {
  // Training plan objective function
  def objFunc(
    t: UserTraits, 
    activityType: ActivityType, 
    preDefinedActivities: List[TrainingPlanActivity] = Nil,
    partiallyDefinedActivities: List[TrainingPlanActivity] = Nil
  ): ObjectiveFunction = {
    new ObjectiveFunction(
      t, 
      activityType, 
      preDefinedActivities,
      partiallyDefinedActivities)
  }
  // Calculate the level needed to complete a cycling activity
  def cyclingLevel(
    weight: Double, 
    bikeRollingResistance: Double, 
    bikeDrag: Double
  ): ActivityLevelFunction = {
    new CyclingLevelFunction(weight, bikeRollingResistance, bikeDrag)
  }
  // Calculate the level needed to complete a running activity
  def runningLevel(weight: Double): ActivityLevelFunction = {
    new RunningLevelFunction(weight)
  }
  // Calculate the effort of an activity
  def activityEffort(activityType: ActivityType): ActivityEffortFunction = {
    activityType match {
      case ActivityType.Run => new RunningEffortFunction()
      case ActivityType.Ride => new CyclingEffortFunction()
      case _ => throw new RuntimeException("Unsupported activity type")
    }
  }
  // Change training plan from n-dimensial vector into n/3 activity 
  // training plan
  def shapeTrainingPlan(
    trainingPlan: List[Double],
    userID: UUID,
    activityType: ActivityType
  ): List[TrainingPlanActivity] = {
    trainingPlan.sliding(3,3).toList.map{ act =>
      TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = userID,
        activityType = activityType,
        distance = Some(act.head),
        time = Some(act(1)),
        elevation = Some(act(2)),
        createdAt = new DateTime)
    }.toList
  }
  // Change training plan from a n-activity training plan to a 
  // n*3-dimensional vector
  def flattenTrainingPlan(
    trainingPlan: List[TrainingPlanActivity]
  ): List[Double] = {
    trainingPlan.flatMap(a => List(a.distance, a.time, a.elevation).flatten)
  }
  // If there are user defined activities add them to the training plan to be 
  // evaluated
  def interpretTrainingPlan(userID: UUID, activityType: ActivityType)(
    partiallyDefinedActivities: List[TrainingPlanActivity], 
    psoPlan: List[Double]
  ): List[TrainingPlanActivity] = {
    partiallyDefinedActivities match {
      case Nil => 
        TrainingPlanUtil.shapeTrainingPlan(psoPlan, userID, activityType)
      case acts =>
        TrainingPlanUtil.shapeTrainingPlan(
          interweaveUserDefinitions(acts, psoPlan), 
          userID, 
          activityType)
    }
  }
  // Take a list of user defined activities and put them in the right place in a 
  // training plan. The first n values in the psoPlan fill in the n empty values
  // in the user defined activities
  def interweaveUserDefinitions(
    userDefinedActivities: List[TrainingPlanActivity],
    psoPlan: List[Double], 
    trainingPlan: List[Double] = Nil
  ): List[Double] = {
    userDefinedActivities match {
      case Nil => trainingPlan ++ psoPlan
      case act::tail =>
        val (distance, psoPlanMinusDistance) = act.distance match {
          case None => (psoPlan.head, psoPlan.drop(1))
          case Some(d) => (d, psoPlan)
        }
        val (time, psoPlanMinusTime) = act.time match {
          case None => (psoPlanMinusDistance.head, psoPlanMinusDistance.drop(1))
          case Some(d) => (d, psoPlanMinusDistance)
        }
        val (elevation, psoPlanMinusElevation) = act.elevation match {
          case None => (psoPlanMinusTime.head, psoPlanMinusTime.drop(1))
          case Some(d) => (d, psoPlanMinusTime)
        }
        interweaveUserDefinitions(
          tail, 
          psoPlanMinusElevation, 
          trainingPlan ++ List(distance, time, elevation))
    }
  }
  // Heaviside step function
  def heaviside(x: Double): Double = { if (x < 0) 0 else x }

  // Handle optional distance/time/elevations
  def get(value: Option[Double]): Double = {
    value.getOrElse(0.0)
  }

  // Count the number of short, average, and long activities
  def countActivitiesByLength(
    tp: List[TrainingPlanActivity], 
    activityType: ActivityType
  ): Map[ActivityLength, Double] = {
    activityType match {
      case ActivityType.Run => Map(
        ActivityLength.Short -> 
          tp.count(a => get(a.time) >= 15 && get(a.time) < 45).toDouble,
        ActivityLength.Average -> 
          tp.count(a => get(a.time) >= 45 && get(a.time) <= 90).toDouble,
        ActivityLength.Long -> 
          tp.count(a => get(a.time) > 90).toDouble)
      case ActivityType.Ride => Map(
        ActivityLength.Short -> 
          tp.count(a => get(a.time) >= 30 && get(a.time) < 60).toDouble,
        ActivityLength.Average -> 
          tp.count(a => get(a.time) >= 60 && get(a.time) <= 120).toDouble,
        ActivityLength.Long -> 
          tp.count(a => get(a.time) > 120).toDouble)
      case _ => throw new RuntimeException("Unsupported activity type")
    }
  }
  // Get the number of short, average, and long activities for a variance
  // given the total number of activities
  def varianceFractionToCount(
    variance: ActivityVariance, 
    numberOfActivities: Int
  ): Map[ActivityLength, Int] = {
    Map(
      ActivityLength.Short -> 
        math.floor(numberOfActivities * variance.fractionShort).toInt,
      ActivityLength.Average -> 
        math.ceil(numberOfActivities * variance.fractionAverage).toInt,
      ActivityLength.Long -> 
        math.floor(numberOfActivities * variance.fractionLong).toInt)
  }
  // Build UserTraits from UserPhysicalAttributes and UserActivityAttributes
  def buildUserTraits(
    pa: UserPhysicalAttributes, 
    actAttr: Seq[UserActivityAttributes]
  ): UserTraits = {
    val aa: Map[String, UserActivityAttributes] = actAttr.map{ l => l.activityType.toString -> l}.toMap
    UserTraits (
      userID = pa.userID,
      height = pa.height,
      weight = pa.weight,
      cyclingDays = aa("Ride").days,
      runningDays = aa("Run").days,
      cyclingActivities = aa("Ride").activities,
      runningActivities = aa("Run").activities,
      cyclingLevel = aa("Ride").level,
      runningLevel = aa("Run").level,
      cyclingVariance = aa("Ride").variance,
      runningVariance = aa("Run").variance,
      cyclingActivityLengthRange = aa("Ride").activityLengthRange,
      runningActivityLengthRange = aa("Run").activityLengthRange,
      bikeRollingResistance = 0.004, 
       // aa("Ride").activitySpecific("bikeRollingResistance"),
      //bikeDrag = aa("Ride").activitySpecific("bikeDrag"),
      bikeDrag = 1.0,
      furthestDistanceBike = aa("Ride").longestDistance,
      furthestDistanceRun = aa("Run").longestDistance)
  }
}

