package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import org.scalatest.WordSpecLike
import resources.DataHelper._
import services.trainingplan.TrainingPlanUtil
import com.trifectalabs.osprey.v0.models.ActivityType
import org.joda.time.DateTime
import java.util.UUID

class RunningEffortFunctionTest extends WordSpecLike {
  "The running effort function" should {
    "calculate the effort of a short run" in {
      val expectedEffort = 80.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Run)(validRunningTrainingPlan.head)
      assert(effort == expectedEffort)
    }

    "calculate the effort of an average run" in {
      val expectedEffort = 180.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Run)(validRunningTrainingPlan(1))
      assert(effort == expectedEffort)
    }

    "calculate the effort of a long run" in {
      val sampleActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        activityType = ActivityType.Run,
        distance = Some(20.0),
        time = Some(120.0),
        elevation = Some(40.0),
        createdAt = new DateTime)
      val expectedEffort = 330.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Run)(sampleActivity)
      assert(effort == expectedEffort)
    }
  }
}
