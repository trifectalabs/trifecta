package services

import com.trifectalabs.osprey.v0.models.{PerformanceInfo, SingleActivity}
import modules._
import com.trifectalabs.raven.v0.models._
import org.joda.time.{Days, DateTime, Interval}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID
import tables._

class FitnessPointsService {

  /*
   * One point for time spent in zone 1, two points for time in zone 2, etc.
   */
  def getEffortFromActivityZones(zones: ActivityZones): Double = {
    (zones.zoneOne/60) + (2 * (zones.zoneTwo/60)) + (3 * (zones.zoneThree/60)) +
      (4 * (zones.zoneFour/60)) + (5 * (zones.zoneFive/60))
  }
  
  def getMostRelevantActivityZonesForActivity(
    activityID: UUID 
  ): Future[ActivityZones] = {
    ActivityZonesTable.findByID(activityID).map { actZones =>
      actZones.exists(_.zoneType == ZoneType.Power) match {
        case true => actZones.filter(_.zoneType == ZoneType.Power).head
        case false => {
          actZones.exists(_.zoneType == ZoneType.Hr) match {
            case true => actZones.filter(_.zoneType == ZoneType.Hr).head
            case false => {
              actZones.exists(_.zoneType == ZoneType.Pace) match {
                case true => actZones.filter(_.zoneType == ZoneType.Pace).head
                case false => throw new RuntimeException(s"No activity zones for activity $activityID")
              }
            }
          }
        }
      }
    }
  }

  def getPointsForActivity(activityID: UUID): Future[Double] = {
    getMostRelevantActivityZonesForActivity(activityID) map { z =>
      getEffortFromActivityZones(z)
    }
  }

  def getInitLevelOfUser(acts: List[SingleActivity]): Future[Double] = {
    acts match {
      case Nil => Future(0.0)
      case _ =>
        // Ensure activities are sorted
        val sorted = (acts, acts.tail).zipped.forall{ case (a1, a2) => 
          a1.startTime.isBefore(a2.startTime)
        }
        val sortedActs = if (!sorted) {
          acts.sortWith{ case (a, b) => a.startTime.isBefore(b.startTime) } 
        } else {
          acts
        }
        // Move start time back to 12AM to look at full days
        val startTime = sortedActs.head.startTime
          .minusMillis(sortedActs.head.startTime.millisOfDay().get())
        val daysBetween = 
          Days.daysBetween(startTime, new DateTime).getDays + 1
        val days = (0 until daysBetween).map(d => startTime.plusDays(d)).toList
        evaluateFitnessOverDaysWithActivities(days, sortedActs)
    }
  }

  def evaluateFitnessOverDaysWithActivities(
    days: List[DateTime],
    acts: List[SingleActivity],
    currentFitness: Double = 0.0
  ): Future[Double] = {
    acts match {
      // no activities left
      case Nil =>
        days match {
          case Nil => Future(currentFitness)
          case day::tail =>
            evaluateFitnessOverDaysWithActivities(
              tail,
              acts,
              decay(currentFitness))
        }
      case act::tail =>
        val today = new Interval(days.head, days.head.plusDays(1))
        act.startTime match {
          // the activity occurs on this day, award points
          case s if today.contains(s) =>
            getPointsForActivity(acts.head.id).flatMap { points =>
              val pts = points / 100
              evaluateFitnessOverDaysWithActivities(
                days,
                tail,
                currentFitness + pts)
            }
          // else complete daily fitness decay
          case _ =>
            evaluateFitnessOverDaysWithActivities(
              days.tail,
              acts,
              decay(currentFitness))
        }
    }
  }

  // decrease current fitness by one day
  def decay(currentFitness: Double): Double = {
    math.exp(-1.0/50) * currentFitness
  }
}

