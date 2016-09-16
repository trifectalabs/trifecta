package services.trainingplan.functions

import org.scalatest.WordSpecLike
import resources.DataHelper._
import services.trainingplan.TrainingPlanUtil
import com.trifectalabs.osprey.v0.models.ActivityType

class ObjectiveFunctionTest extends WordSpecLike {
  "The training plan objective function" should {
    "evaluate a cycling training plan" in {
      val sampleTrainingPlan = List(20.0, 40.0, 150.0, 40.0, 90.0, 250.0)
      val expectedScore = 370.0
      val score = TrainingPlanUtil.objFunc(
        validUserAttributes1, ActivityType.Ride)(sampleTrainingPlan)
      assert(score == expectedScore)
    }

    "evaluate a running training plan" in {
      val sampleTrainingPlan = List(5.0, 30.0, 15.0, 10.0, 60.0, 25.0)
      val expectedScore = 258.49999999999994
      val score = TrainingPlanUtil.objFunc(
        validUserAttributes1, ActivityType.Run)(sampleTrainingPlan)
      assert(score == expectedScore)
    }

    "evaluate a training plan with a fully defined activity" in {
      val sampleTrainingPlan = List(10.0, 60.0, 25.0)
      val expectedScore = 258.49999999999994
      val score = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Run,
        List(),
        List(validUserDefinedActivity1))(sampleTrainingPlan)
      assert(score == expectedScore)
    }

    "evaluate a training plan with a partially defined activity missing a field" in {
      val sampleTrainingPlan = List(5.0, 10.0, 60.0, 25.0)
      val expectedScore = 258.49999999999994
      val score = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Run, 
        List(),
        List(validUserDefinedActivity2))(sampleTrainingPlan)
      assert(score == expectedScore)
    }

    "evaluate a training plan with a partially defined activity missing two fields" in {
      val sampleTrainingPlan = List(5.0, 15.0, 10.0, 60.0, 25.0)
      val expectedScore = 258.49999999999994
      val score = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Run,
        List(),
        List(validUserDefinedActivity3))(sampleTrainingPlan)
      assert(score == expectedScore)
    }

    "calculate the recovery cost of a cycling training plan" in {
      val sampleDays = 1
      val expectedRecoveryCost = 425.00000000000006
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Ride)
          .recoveryCost(validCyclingTrainingPlan, sampleDays, ActivityType.Ride)
      assert(cost == expectedRecoveryCost)
    }

    "calculate the recovery cost of a running training plan" in {
      val sampleDays = 1
      val expectedRecoveryCost = 150.00000000000003
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Run)
          .recoveryCost(validRunningTrainingPlan, sampleDays, ActivityType.Run)
      assert(cost == expectedRecoveryCost)
    }

    "calculate the feasibility cost of a cycling training plan" in {
      val sampleFitnessLevel = 25.0
      val expectedFeasibilityCost = 718.4250927138072
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Ride).feasibilityCost(
          validCyclingTrainingPlan,
          sampleFitnessLevel,
          TrainingPlanUtil.cyclingLevel(
            validUserAttributes1.weight,
            validUserAttributes1.bikeRollingResistance,
            validUserAttributes1.bikeDrag))
      assert(cost == expectedFeasibilityCost)
    }

    "calculate the feasibility cost of a running training plan" in {
      val sampleFitnessLevel = 10.0
      val expectedFeasibilityCost = 1070.5
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Ride).feasibilityCost(
          validRunningTrainingPlan,
          sampleFitnessLevel,
          TrainingPlanUtil.runningLevel(validUserAttributes1.weight))
      assert(cost == expectedFeasibilityCost)
    }

    "calculate the variance cost of a cycling training plan" in {
      val sampleShortFraction = 0.0
      val sampleAverageFraction = 0.0
      val sampleLongFraction = 1.0
      val expectedVarianceCost = 12750.0
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Ride).varianceCost(
          validCyclingTrainingPlan,
          ActivityType.Ride,
          sampleShortFraction,
          sampleAverageFraction,
          sampleLongFraction)
      assert(cost == expectedVarianceCost)
    }

    "calculate the variance cost of a running training plan" in {
      val sampleShortFraction = 0.0
      val sampleAverageFraction = 0.0
      val sampleLongFraction = 1.0
      val expectedVarianceCost = 12750.0
      val cost = TrainingPlanUtil.objFunc(
        validUserAttributes1, 
        ActivityType.Ride).varianceCost(
          validRunningTrainingPlan,
          ActivityType.Run,
          sampleShortFraction,
          sampleAverageFraction,
          sampleLongFraction)
      assert(cost == expectedVarianceCost)
    }
  }
}
