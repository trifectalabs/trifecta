package services

import com.trifectalabs.osprey.v0.models._
import com.trifectalabs.raven.v0.models._
import services._
import modules._
import resources._
import kiambogo.scrava.Client
import net.liftweb.json.DefaultFormats
import org.joda.time.DateTime
import tables._
import resources.Contexts._
import java.util.UUID
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

class StravaAuthService(scravaClient: Client) extends RavenModule 
{
  implicit val formats = DefaultFormats
  val modelRemapper = new ModelRemapper
  val streams = List(
    "time",
    "latlng",
    "distance",
    "altitude",
    "velocity_smooth",
    "heartrate",
    "cadence",
    "watts_calc",
    "temp",
    "moving",
    "grade_smooth").mkString(",")

  def synchronizeNewAthleteActivities(userID: UUID): Any = {
    val hrZones = TrainingZonesForm(
      Some(TrainingZone(-1, 120)),
      Some(TrainingZone(120, 140)),
      Some(TrainingZone(140, 160)),
      Some(TrainingZone(160, 180)),
      Some(TrainingZone(180, -1)))
    val paceZones = TrainingZonesForm(
      Some(TrainingZone(390, -1)),
      Some(TrainingZone(330, 390)),
      Some(TrainingZone(300, 330)),
      Some(TrainingZone(270, 300)),
      Some(TrainingZone(-1, 270)))
    val powerZones = TrainingZonesForm(
      Some(TrainingZone(-1, 50)),
      Some(TrainingZone(50, 150)),
      Some(TrainingZone(150, 250)),
      Some(TrainingZone(250, 450)),
      Some(TrainingZone(450, -1)))
    trainingZonesClient.postByUserIDAndZoneType(
      userID, ZoneType.Hr, hrZones)
    trainingZonesClient.postByUserIDAndZoneType(
      userID, ZoneType.Pace, paceZones)
    trainingZonesClient.postByUserIDAndZoneType(
      userID, ZoneType.Power, powerZones)
    Future.sequence(
      scravaClient.listAthleteActivities(retrieveAll = true)
        .filter(a => 
            a.`type` == "Run" ||
            a.`type` == "Ride" ||
            a.`type` == "Swim") 
        .filter(a => a.manual == false) map { activity =>
          val (act, perf) = 
            modelRemapper.scravaActivityToSlickActivity(activity, userID)
            for {
              s <- Future(scravaClient.retrieveActivityStream(activity.id.toString) foreach (str =>
                  StreamTable.add(StreamPattern.matchStream(str, act.id))))(activityFetcherContext)
              p <- PerformanceInfoTable.add(perf)
              a <- SingleActivitiesTable.add(act)
              b <- activityZonesClient.post(act)
            } yield { 
              println(act.id)
            }
 }
            //}(Contexts.activityFetcherContext)
  ) onComplete {
    case Success(a) =>
      // Let raven calm the fuck down
      println("calculating levels")
      Thread.sleep(5000)
      List(ActivityType.Run, ActivityType.Ride, ActivityType.Swim)
        .foreach(t => calculateInitialAthleteLevel(userID, t))
    case Failure(e) => throw new RuntimeException(e)
  }
}

  def calculateInitialAthleteLevel(userID: UUID, activityType: ActivityType) = Future {
    val futActivities = activityType match {
      case ActivityType.Ride =>
        for {
          acts <- SingleActivitiesTable.findAllByUserID(userID)
          rides = acts.toList.filter(_.activityType == ActivityType.Ride)
          virtualRides = acts.toList.filter(_.activityType == ActivityType.VirtualRide)
          } yield {
            rides ++ virtualRides
          }
      case _ =>
        SingleActivitiesTable.findAllByUserID(userID).map(_.toList
          .filter(_.activityType == activityType))
    }
    futActivities.flatMap { activities =>
      activityZonesClient.postLevelByUserIDAndActivityType(userID, activityType, List())
        .map { level => 
          UserActivityAttributesTable
            .findActiveByUserIDAndActivityType(userID, activityType)
            .map (_ match {
              case Some(attr) =>
                UserActivityAttributesTable.remove(attr)
                UserActivityAttributesTable.add(attr.copy(level = level))
                println(s"Updated $userID level $level for $activityType")
              case None =>
                UserActivityAttributesTable.add(UserActivityAttributes(
                  userID = userID,
                  activityType = activityType,
                  level = level,
                  days = 14,
                  activities = 8,
                  longestDistance = 0.0,
                  activityLengthRange = Map(
                    "short" -> ActivityLengthRange(30, 60),
                    "average" -> ActivityLengthRange(60, 120),
                    "long" -> ActivityLengthRange(120, 360)),
                  variance = ActivityVariance(0.5, 0.5, 0.0),
                  activitySpecific = Map(),
                  createdAt = new DateTime))
                println(
                  s"Saved new $activityType attributes for $userID with level $level")
            })
        }
    }
  }
}
