package services.trainingplan.functions

import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import services.trainingplan.TrainingPlanUtil

class CyclingLevelFunction(weight: Double, bikeRollingResistance: Double, bikeDrag: Double) extends ActivityLevelFunction {
  val get = TrainingPlanUtil.get _
  def apply(a: TrainingPlanActivity): Double = {
    val p = power(a)
    val time = get(a.time)
    // Power curve estimation based on the following document
    // http://veloclinic.com/wp-content/uploads/2014/04/PowerModelDerivation-1.pdf
    // the following equation is derived from the equation at the top of the
    // third page of the document
    // p = (100 + (lvl - 1) * 20) / (1 + (t / 60)) + (1 + (lvl - 1) * 0.2) / (1 + t)
    // rearrange for lvl
    (5 * p * (math.pow(time, 2) + 61 * time + 60) - 4 *
      (6001 * time + 6060)) / (6001 * time + 6060)
  }

  // Calculate the power needed to complete a ride
  def power(a: TrainingPlanActivity): Double = {
    val distance = get(a.distance)
    val time = get(a.time)
    val elevation = get(a.elevation)
    val velocity = (distance * 1000) / (time * 60)
    val theta = math.asin(elevation / (distance * 1000 / 3))
    // https://en.wikipedia.org/wiki/Normal_force
    val normalHill = math.cos(theta) * weight * 9.8
    val normalFlat = weight * 9.8
    // http://en.wikipedia.org/wiki/Rolling_resistance
    val powerRRHill = bikeRollingResistance * normalHill * velocity
    val powerRRFlat = bikeRollingResistance * normalFlat * velocity
    // https://en.wikipedia.org/wiki/Density_of_air
    val ro = 1.255
    // assuming constant frontal surface area
    val area = 0.5
    val powerWind = 0.5 * ro * math.pow(velocity, 3) * bikeDrag * area
    val powerGravity = weight * 9.8 * math.sin(theta) * velocity
    val powerUphill = powerRRHill + powerWind + powerGravity
    val powerFlat = powerRRFlat + powerWind
    val powerDownhill = powerRRHill + powerWind - powerGravity
    val p = (powerUphill + powerFlat + powerDownhill) / 3
    if (p.isNaN) 0 else p
  }
}
