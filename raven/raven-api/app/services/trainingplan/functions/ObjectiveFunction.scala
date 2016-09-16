package services.trainingplan.functions

import models.UserTraits
import services.trainingplan.TrainingPlanUtil
import com.trifectalabs.osprey.v0.models.ActivityType
import com.trifectalabs.raven.v0.models.{ActivityLength, TrainingPlanActivity}

class ObjectiveFunction(
  t: UserTraits, 
  activityType: ActivityType, 
  preDefinedActivities: List[TrainingPlanActivity],
  partiallyDefinedActivities: List[TrainingPlanActivity]
) extends Function1[List[Double], Double] {
  def apply(trainingPlan: List[Double]) = {
    val tp = preDefinedActivities ++
      TrainingPlanUtil.interpretTrainingPlan(
        t.userID, activityType)(partiallyDefinedActivities, trainingPlan)
    // Calculate the fitness to be gained from completing the training plan
    val fitness = tp.foldLeft[Double](0.0)((f, a) => 
        f + TrainingPlanUtil.activityEffort(activityType)(a))
    val (getLevel, fitnessLevel, days, variance) = activityType match {
      case ActivityType.Run => (
        TrainingPlanUtil.runningLevel(t.weight),
        t.runningLevel, 
        t.runningDays,
        t.runningVariance)
      case ActivityType.Ride => (
        TrainingPlanUtil.cyclingLevel(
          t.weight, 
          t.bikeRollingResistance, 
          t.bikeDrag), 
        t.cyclingLevel, 
        t.cyclingDays, 
        t.cyclingVariance)
      case _ => throw new RuntimeException("Unsupported activity type")
    }
    // Penalize the plan for being not being accomplishable in the specified 
    // time frame, for being unfeasible for the athlete, or for not aligning to
    // the athletes preferences
    fitness - recoveryCost(tp, days, activityType) -
      feasibilityCost(tp, fitnessLevel, getLevel) -
      varianceCost(
        tp, 
        activityType, 
        variance.fractionShort, 
        variance.fractionAverage, 
        variance.fractionLong)
  }
  // Calculate the cost for a training plan due to athlete recovery time
  def recoveryCost(
    tp: List[TrainingPlanActivity], 
    days: Int, 
    activityType: ActivityType
  ): Double = {
    val recoveryTime = tp.foldLeft[Double](0.0)((r, a) => 
        r + TrainingPlanUtil.activityEffort(activityType)(a)/200)
    500 * TrainingPlanUtil.heaviside(recoveryTime - days.toDouble)
  }
  // Calculate the cost for a training plan due to feasibility for the athlete
  def feasibilityCost(tp: List[TrainingPlanActivity], fitnessLevel: Double,
    activityLevel: TrainingPlanActivity => Double): Double = {
    val totalLevels = tp.foldLeft[Double](0.0){(l, a) =>
      val level = activityLevel(a)
      // calculate range of allowable activities
      val upperLimit = fitnessLevel + 1
      val lowerLimit = if (fitnessLevel < 5) 1 else fitnessLevel - 4
      val levelPenalty =
        // above allowed range
        if (level > upperLimit) {
          level - upperLimit
        // below allowed range
        } else if (level < lowerLimit) {
          lowerLimit - level
        // acceptable range
        } else {
          0
        }
      l + levelPenalty
    }
    TrainingPlanUtil.heaviside(50 * totalLevels)
  }
  // Calculate the cost of a training plan due to lack of alignment with user's
  // time variance preferences
  def varianceCost(
    tp: List[TrainingPlanActivity], 
    activityType: ActivityType,
    fractionShort: Double, 
    fractionAverage: Double, 
    fractionLong: Double
  ): Double = {
    val counts = TrainingPlanUtil.countActivitiesByLength(tp, activityType)
    val total = tp.length.toDouble
    val calculatedShortFraction = counts(ActivityLength.Short) / total
    val calculatedAverageFraction = counts(ActivityLength.Average) / total
    val calculatedLongFraction = counts(ActivityLength.Long) / total
    // penalize if more than 10 percent over or under perferred percentage
    // of that length of activity
    val shortPenalty = if (calculatedShortFraction >= fractionShort + 0.1) {
      7500 * (calculatedShortFraction - (fractionShort + 0.1))
    } else if (calculatedShortFraction <= fractionShort - 0.1) {
      7500 * ((fractionShort - 0.1) - calculatedShortFraction)
    } else {
      0
    }
    val avgPenalty = if (calculatedAverageFraction >= fractionAverage + 0.1) {
      7500 * (calculatedAverageFraction - (fractionAverage + 0.1))
    } else if (calculatedAverageFraction <= fractionAverage - 0.1) {
      7500 * ((fractionAverage - 0.1) - calculatedAverageFraction)
    } else {
      0
    }
    val longPenalty = if (calculatedLongFraction >= fractionLong + 0.1) {
      7500 * (calculatedLongFraction - (fractionLong + 0.1))
    } else if (calculatedLongFraction <= fractionLong - 0.1) {
      7500 * ((fractionLong - 0.1) - calculatedLongFraction)
    } else {
      0
    }
    shortPenalty + avgPenalty + longPenalty
  }
}

