package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity

trait ActivityEffortFunction extends Function1[TrainingPlanActivity, Double] {
  def apply(a: TrainingPlanActivity): Double
}