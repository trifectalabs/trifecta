package services.functions

import com.trifectalabs.myriad.aco.Path
import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import org.joda.time.{DateTime, Days} 

class DistanceFunction(
   plan: List[TrainingPlanActivity],
   scheduleTimes: List[(DateTime, DateTime)],
   randomSeed: Option[Long] = None
) extends Function4[Int, Int, Int, List[Path], Double] {
  val r = new scala.util.Random(randomSeed.getOrElse(System.currentTimeMillis))

  def apply(start: Int, finish: Int, index: Int, path: List[Path]) = {
    val nextTime = scheduleTimes(index)
    val span = nextTime._2.getMillis - nextTime._1.getMillis
    if (span < plan(start).time.get * 60000) {
      val d = Double.PositiveInfinity
      // println(s"Activity $start in bucket $index with dist $d")
      d
    } else {
      val act = ((plan(start), scheduleTimes(index)._1), path.length)
      val acts = (path.map(p => (plan(p.begin.id), scheduleTimes(p.index)._1))
        .sortWith{ case ((_, t1), (_, t2)) => t1.isBefore(t2) })
        .zipWithIndex
      //val pathCost = acts.foldLeft(0.0)((cost, act) => cost + dist(act, acts))
      val d = dist(act, acts)
      // println(s"Activity $start in bucket $index with dist $d")
      d
    }
  }

  def recovery(effort: Double, time: Double): Double = {
    if (time < effort/100) {
      -3000000.0 / math.pow(effort, 2) * math.pow(time, 3) +
        45000.0 / effort * math.pow(time, 2)
    } else {
      8 * time + 3 * effort / 2 - 8 * effort / 100
    }
  }

  def effort(a: TrainingPlanActivity): Double = {
    val time = a.time.getOrElse(0.0)
    if (time >= 30 && time <  60) {
      120.0 + r.nextGaussian() * 15.0
    } else if (time >= 60 && time <= 120) {
      250 + r.nextGaussian() * 30.0
    } else {
      2.75 * time
    }
  }

  def dist(
    act: ((TrainingPlanActivity, DateTime), Int),
    acts: List[((TrainingPlanActivity, DateTime), Int)]
  ): Double = {
    val i = act._2
    if (i > 0) {
      val t = act._1._2
      val a = act._1._1
      val prevT = acts(i - 1)._1._2
      val eff = effort(a)
      val daysBetweenActivities =
        Days.daysBetween(prevT.toLocalDate(), t.toLocalDate()).getDays()
      val planned = recovery(eff, daysBetweenActivities)
      val optimal = recovery(eff, eff / 200)
      val cutoff = recovery(eff, 0)
      val diff = math.abs(optimal - planned)
      if (diff > optimal - cutoff) 1.0 else diff
    } else {
      1.0
    }
  }
}
