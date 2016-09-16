package services.trainingplan.functions

import org.scalatest.WordSpecLike
import resources.DataHelper._
import services.trainingplan.TrainingPlanUtil

class CyclingLevelFunctionTest extends WordSpecLike {
  "The cycling level function" should {
    "calculate level needed to complete a ride" in {
      val expectedLevel = 13.083415316777563
      val level = TrainingPlanUtil.cyclingLevel(
        validUserAttributes1.weight,
        validUserAttributes1.bikeRollingResistance,
        validUserAttributes1.bikeDrag)(validCyclingTrainingPlan.head)
      assert(level == expectedLevel)
    }

    "calculate the power required to complete a ride" in {
      val expectedPower = 205.0843175345833
      val power = TrainingPlanUtil.cyclingLevel(
        validUserAttributes1.weight,
        validUserAttributes1.bikeRollingResistance,
        validUserAttributes1.bikeDrag).power(validCyclingTrainingPlan.head)
      assert(power == expectedPower)
    }
  }
}
