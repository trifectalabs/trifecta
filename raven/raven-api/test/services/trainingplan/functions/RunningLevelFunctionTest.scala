package services.trainingplan.functions

import org.scalatest.WordSpecLike
import resources.DataHelper._
import services.trainingplan.TrainingPlanUtil

class RunningLevelFunctionTest extends WordSpecLike {
  "The running level function" should {
    "calculate level needed to complete a run" in {
      val expectedLevel = 22.44
      val level = TrainingPlanUtil.runningLevel(
        validUserAttributes1.weight)(validRunningTrainingPlan(1))
      assert(level == expectedLevel)
    }

    "calculate the power required to complete a run" in {
      val expectedPower = 490.0
      val power = TrainingPlanUtil.runningLevel(
        validUserAttributes1.weight).power(validRunningTrainingPlan(1))
      assert(power == expectedPower)
    }
  }
}
