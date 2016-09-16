package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import services.trainingplan.TrainingPlanUtil

class RunningEffortFunction extends ActivityEffortFunction {
  val get = TrainingPlanUtil.get _
  // Calculate the effort of a run
  def apply(a: TrainingPlanActivity): Double = {
    if (get(a.time) >= 15 && get(a.time) < 45) 80 // short run
    else if (get(a.time) >= 45 && get(a.time) <= 90) 180 // average run
    else 2.75 * get(a.time) // long run
  }
}
