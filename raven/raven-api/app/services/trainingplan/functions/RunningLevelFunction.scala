package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import services.trainingplan.TrainingPlanUtil

class RunningLevelFunction(weight: Double) extends ActivityLevelFunction {
  val get = TrainingPlanUtil.get _
  def apply(a: TrainingPlanActivity): Double = {
    val p = power(a)
    val t = get(a.time)
    // p = (475 + (lvl - 1) * 10) / (1 + (t / 30)) +
    //     (250 + (lvl - 1) * 10) / (1 + (t/ 1200))
    // rearrange for lvl
    (-450*(56400+671*t)+p*(36000+1230*t+math.pow(t,2)))/(300*(2400+41*t))
  }
  def power(a: TrainingPlanActivity): Double = {
    val distance = get(a.distance)
    val time = get(a.time)
    val elevation = get(a.elevation)
    val velocity = (distance * 1000) / (time * 60)
    val theta = math.asin(elevation / (distance * 1000 / 3))
    //http://sprott.physics.wisc.edu/technote/walkrun.htm
    val powerRunning = weight * 9.8 * velocity / 4
    val powerGravity = weight * 9.8 * math.sin(theta) * velocity
    val powerUphill = powerRunning + powerGravity
    val powerFlat = powerRunning
    val powerDownhill = powerRunning - powerGravity
    val p = (powerUphill + powerFlat + powerDownhill) / 3
    if (p.isNaN) 0 else p
  }
}
