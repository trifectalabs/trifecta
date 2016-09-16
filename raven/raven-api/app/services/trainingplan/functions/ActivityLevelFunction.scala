package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity

trait ActivityLevelFunction extends Function1[TrainingPlanActivity, Double] {
  def apply(a: TrainingPlanActivity): Double
  def power(a: TrainingPlanActivity): Double
}
