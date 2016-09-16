package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import org.scalatest.WordSpecLike
import resources.DataHelper._
import services.trainingplan.TrainingPlanUtil
import com.trifectalabs.osprey.v0.models.ActivityType
import org.joda.time.DateTime
import java.util.UUID

class CyclingEffortFunctionTest extends WordSpecLike {
  "The cycling effort function" should {
    "calculate the effort of a short ride" in {
      val expectedEffort = 120.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Ride)(validCyclingTrainingPlan.head)
      assert(effort == expectedEffort)
    }

    "calculate the effort of an average ride" in {
      val expectedEffort = 250.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Ride)(validCyclingTrainingPlan(1))
      assert(effort == expectedEffort)
    }

    "calculate the effort of a long ride" in {
      val sampleActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        activityType = ActivityType.Ride,
        distance = Some(75.0),
        time = Some(180.0),
        elevation = Some(400.0),
        createdAt = new DateTime)
      val expectedEffort = 495.0
      val effort = TrainingPlanUtil.activityEffort(ActivityType.Ride)(sampleActivity)
      assert(effort == expectedEffort)
    }
  }
}
