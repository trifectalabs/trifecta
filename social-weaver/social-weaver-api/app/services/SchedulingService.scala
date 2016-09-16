package services

import play.api.libs.ws._
import play.api.Play.current
import java.util.UUID
import org.joda.time.{DateTime, Minutes}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.{DateTime => GDateTime}
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.trifectalabs.raven.v0.models.TrainingPlanActivity
import com.trifectalabs.osprey.v0.models.ActivityType
import com.trifectalabs.social.weaver.v0.models._
import com.trifectalabs.myriad._
import com.trifectalabs.myriad.aco._
import modules.OspreyModule
import services.functions.DistanceFunction

class SchedulingService(
  randomSeed: Long = System.currentTimeMillis
) extends OspreyModule {
  lazy val jsonFactory = JacksonFactory.getDefaultInstance()
  lazy val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  val applicationName = "social-weaver"

  def createScheduleForTrainingPlan(
    userID: UUID,
    activityType: ActivityType,
    startTime: DateTime,
    plan: List[TrainingPlanActivity]
  ): Future[TrainingSchedule] = {
    getScheduleTimes(userID, activityType, startTime).flatMap{ scheduleTimes =>
      val actCount = plan.length
      val timeCount = scheduleTimes.length
      val antPaths = (0 until actCount)
        .map(x => Seq.fill(timeCount)(x)).flatten
        .map(y => (y, y + 1)).toList
      val sortedPlan = plan.sortWith(_.time.get > _.time.get)
      val conf = ACOConfiguration(
        distanceFunction = new DistanceFunction(sortedPlan, scheduleTimes),
        numberOfAnts = 25,
        numberOfNodes = actCount + 1,
        paths = antPaths,
        start = 0,
        finish = actCount,
        directedPaths = true,
        multiPathCollapse = true,
        pheromoneDecayRate = 0.01,
        terminationCriteria = TerminationCriteria(maxIterations = 1000))
      val acoSystemFactory = new ACOSystemFactory(conf)
      val aco = acoSystemFactory.build()
      val acoJob = new ACOExecutor(aco)
      acoJob.run.map{ case Result(Right(path)) =>
        val schedule = path.map(p => 
          ActivityTime(sortedPlan(p.begin.id).id, scheduleTimes(p.index)._1))
        TrainingSchedule(schedule)
      }
    }
  }

  // TODO: handle case where lack of events makes huge time slots
  def getScheduleTimes(
    userID: UUID,
    activityType: ActivityType,
    startTime: DateTime
  ): Future[List[(DateTime, DateTime)]] = {
    for {
      user <- userClient.getById(userID)
      attr <- userActivityAttributesClient.getByUserID(userID).map { actAttrs =>
        actAttrs.filter(_.activityType == activityType).head
      }
    } yield {
      val accessToken = user.googleCalendarAccessToken.get
      val calID = user.googleCalendarIDs.get.head
      val daysInPlan = attr.days
      val endTime = startTime.plusDays(daysInPlan)

      val credential = new GoogleCredential().setAccessToken(accessToken)
      val client = new com.google.api.services.calendar.Calendar
        .Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(applicationName)
        .build()
    
      val events = getEvents(client, calID, startTime, endTime)
      val busyTimes = parseEvents(events)
      findFreeTime(busyTimes, startTime, endTime)
    }
  }

  private def getEvents(
    client: Calendar,
    // TODO: support multiple calendars
    calID: String,
    startTime: DateTime,
    endTime: DateTime
  ): List[Event] = {
    val timeMin = new GDateTime(startTime.getMillis)
    val timeMax = new GDateTime(endTime.getMillis) 
    var pageToken: String = null
    var counter = 0
    Iterator.continually {
      counter = counter + 1
      val req = client.events().list(calID)
        .setPageToken(pageToken)
        .setTimeMin(timeMin)
        .setTimeMax(timeMax)
        .execute()
      pageToken = req.getNextPageToken()
      req.getItems().asScala
    }.takeWhile(_ => pageToken != null || counter == 1).toList.flatten
      .map(event => client.events().instances(calID, event.getId())
        .setTimeMin(timeMin)
        .setTimeMax(timeMax)
        .execute()
        .getItems()
        .asScala).flatten
  }

  private def parseEvents(events: List[Event]): List[DateTime] = {
    val eventTimes = events.filter(e => e.getStart().getDate() == null)
      .map(e => List(
        new DateTime(e.getStart().getDateTime().getValue()),
        new DateTime(e.getEnd().getDateTime().getValue())))
          .sortWith((e1, e2) => e1.head.isBefore(e2.head))
    eventTimes.foldLeft(List[List[DateTime]]())(
      (times, next) =>
        times match {
          case Nil => 
            List(next)
          case head::tail if (next.head.isBefore(head.last)) => 
            List(head.head, next.last)::tail
          case t =>
            next::t
        }).reverse.flatten
  }

  private def findFreeTime(
    busyTimes: List[DateTime],
    startTime: DateTime,
    endTime: DateTime
  ): List[(DateTime, DateTime)] = {
    val freeTimes = if (busyTimes.last.isAfter(endTime)) {
      startTime :: busyTimes.dropRight(1)
    } else {
      startTime :: (busyTimes :+ endTime)
    }
    freeTimes.sliding(2, 2)
      .map(t => (t.head, t.last))
      .filter(t => Minutes.minutesBetween(t._1, t._2).getMinutes() >= 30)
      .toList
  }
}
