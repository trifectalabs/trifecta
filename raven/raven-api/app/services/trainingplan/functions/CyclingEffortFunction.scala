package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import services.trainingplan.TrainingPlanUtil

class CyclingEffortFunction extends ActivityEffortFunction {
  val get = TrainingPlanUtil.get _
  // Calculate the effort of a ride
  def apply(a: TrainingPlanActivity): Double = {
    if (get(a.time) >= 30 && get(a.time) < 60) 120 // short activity
    else if (get(a.time) >= 60 && get(a.time) <= 120) 250 // average activity
    else 2.75 * get(a.time) // long activity
  }
}
