package services.trainingplan

import com.trifectalabs.raven.v0.models.{ActivityLength}
import org.scalatest.WordSpecLike
import resources.DataHelper._
import com.trifectalabs.osprey.v0.models.{ActivityType, ActivityVariance}
import java.util.UUID

class TrainingPlanUtilTest extends WordSpecLike {
  "The training plan utility" should {
    "convert variance fractions to activity counts" in {
      val sampleActCount = 8
      val sampleVariance = ActivityVariance(0.25, 0.5, 0.25)
      val expectedCounts = Map(
        ActivityLength.Short -> 2, 
        ActivityLength.Average -> 4, 
        ActivityLength.Long -> 2)
      val actualCounts = 
        TrainingPlanUtil.varianceFractionToCount(sampleVariance, sampleActCount)
      assert(actualCounts == expectedCounts)
    }

    "shape a flattened training plan" in {
      val sampleFlatTrainingPlan = List(20.0, 40.0, 150.0, 40.0, 90.0, 250.0)
      val expectedShapedTrainingPlan = validCyclingTrainingPlan
      val shapedTrainingPlan = TrainingPlanUtil.shapeTrainingPlan(
        sampleFlatTrainingPlan, 
        userID = UUID.randomUUID, 
        activityType = ActivityType.Ride)
      expectedShapedTrainingPlan.zip(shapedTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID,
            createdAt = expected.createdAt) == expected)
      }
    }

    "flatten a training plan" in {
      val expectedFlatTrainingPlan = List(20.0, 40.0, 150.0, 40.0, 90.0, 250.0)
      val flatTrainingPlan =
        TrainingPlanUtil.flattenTrainingPlan(validCyclingTrainingPlan)
      assert(flatTrainingPlan == expectedFlatTrainingPlan)
    }

    "interpret a training plan with no user defined activites" in {
      val psoTrainingPlan = List(5.0, 30.0, 15.0, 10.0, 60.0, 25.0)
      val expectedTrainingPlan = validRunningTrainingPlan
      val actualTrainingPlan = TrainingPlanUtil.interpretTrainingPlan(
        validUserAttributes1.userID,
        activityType = ActivityType.Run)(List(), psoTrainingPlan)
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id, 
            userID = expected.userID,
            createdAt = expected.createdAt) == expected)
      }
    }

    "interpret a training plan with user defined activities" in {
      val psoTrainingPlan = List(5.0, 10.0, 60.0, 25.0)
      val expectedTrainingPlan = validRunningTrainingPlan
      val actualTrainingPlan = TrainingPlanUtil.interpretTrainingPlan(
        validUserAttributes1.userID, 
        activityType = ActivityType.Run)(
        List(validUserDefinedActivity2),
        psoTrainingPlan)
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID,
            createdAt = expected.createdAt) == expected)
      }
    }

    "interweave user defined activities with pso training plan" in {
      val psoTrainingPlan =
        List(5.0, 30.0, 15.0, 1.0, 1.0, 1.0)
      val expectedTrainingPlan =
        List(5.0, 30.0, 15.0, 30.0, 30.0, 15.0, 1.0, 1.0, 1.0)
      val actualTrainingPlan = TrainingPlanUtil.interweaveUserDefinitions(
        List(validUserDefinedActivity2, validUserDefinedActivity3), 
        psoTrainingPlan)
      assert(expectedTrainingPlan == actualTrainingPlan)
    }

    "take the heaviside of a negative value" in {
      assert(TrainingPlanUtil.heaviside(-1) == 0)
    }

    "take the heaviside of a positive value" in {
      assert(TrainingPlanUtil.heaviside(1) == 1)
    }

    "count the different lengths of activities in a cycling training plan" in {
      val expectedCounts = Map(
        ActivityLength.Short -> 1.0, 
        ActivityLength.Average -> 1.0, 
        ActivityLength.Long -> 0.0)
      val actualCounts = TrainingPlanUtil.countActivitiesByLength(
        validCyclingTrainingPlan, 
        ActivityType.Ride)
      assert(actualCounts == expectedCounts)
    }

    "count the different lengths of activities in a running training plan" in {
      val expectedCounts = Map(
        ActivityLength.Short -> 1.0, 
        ActivityLength.Average -> 1.0, 
        ActivityLength.Long -> 0.0)
      val actualCounts = TrainingPlanUtil.countActivitiesByLength(
        validRunningTrainingPlan, 
        ActivityType.Run)
      assert(actualCounts == expectedCounts)
    }
  }
}
