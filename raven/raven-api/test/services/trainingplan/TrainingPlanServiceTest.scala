package services.trainingplan

import com.trifectalabs.raven.v0.models.{ActivityLength, TrainingPlanActivity}
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import resources.DataHelper._
import com.trifectalabs.osprey.v0.models.ActivityType
import org.joda.time.DateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class TrainingPlanServiceTest extends WordSpecLike with ScalaFutures {
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))
  val tpService = new TrainingPlanService()
  val seededService = new TrainingPlanService(0)
  
  "The training plan service" should {
    "generate a near-optimal cycling training plan" in {
      val score = tpService.buildTrainingPlan(
        validUserAttributes2, 
        ActivityType.Ride, 
        List(),
        List()).map(tp =>
          TrainingPlanUtil.objFunc(validUserAttributes2, ActivityType.Ride)(
            TrainingPlanUtil.flattenTrainingPlan(tp)))
      assert(score.futureValue > 2799.0)
    }

    "generate a near-optimal running training plan" in {
      val score = tpService.buildTrainingPlan(
        validUserAttributes2, 
        ActivityType.Run, 
        List(),
        List()).map(tp =>
        TrainingPlanUtil.objFunc(validUserAttributes2, ActivityType.Run)(
          TrainingPlanUtil.flattenTrainingPlan(tp)))
      assert(score.futureValue > 1719.0)
    }

    "generate a near-optimal cycling training plan with predefined activity" in {
      val score = tpService.buildTrainingPlan(
        validUserAttributes2, 
        ActivityType.Ride, 
        List(),
        List()).map(tp =>
        TrainingPlanUtil.objFunc(validUserAttributes2, ActivityType.Ride)(
          TrainingPlanUtil.flattenTrainingPlan(tp)))
      assert(score.futureValue > 1899.0)
    }

    "generate a near-optimal running training plan with predefined activity" in {
      val score = tpService.buildTrainingPlan(
        validUserAttributes2, 
        ActivityType.Run, 
        List(),
        List()).map(tp =>
        TrainingPlanUtil.objFunc(validUserAttributes2, ActivityType.Run)(
          TrainingPlanUtil.flattenTrainingPlan(tp)))
      assert(score.futureValue > 1389.0)
    }

    "get search space for a cycling training plan" in {
      val expectedSearchSpace = List(
        (5.0, 50.0), (30.0, 60.0), (50.0, 450.0),
        (5.0, 50.0), (30.0, 60.0), (50.0, 450.0),
        (10.0, 100.0), (60.0, 120.0), (250.0, 1000.0),
        (10.0, 100.0), (60.0, 120.0), (250.0, 1000.0),
        (10.0, 100.0), (60.0, 120.0), (250.0, 1000.0),
        (10.0, 100.0), (60.0, 120.0), (250.0, 1000.0),
        (20.0, 180.0), (120.0, 360.0), (500.0, 2000.0),
        (20.0, 180.0), (120.0, 360.0), (500.0, 2000.0))
      val actualSearchSpace = 
        tpService.activitySearchSpace(ActivityType.Ride, validUserAttributes2)
      assert(actualSearchSpace == expectedSearchSpace)
    }

    "get search space for a running training plan" in {
      val expectedSearchSpace = List(
        (1.0, 15.0), (15.0, 45.0), (0.0, 75.0),
        (1.0, 15.0), (15.0, 45.0), (0.0, 75.0),
        (5.0, 30.0), (45.0, 90.0), (50.0, 250.0),
        (5.0, 30.0), (45.0, 90.0), (50.0, 250.0),
        (5.0, 30.0), (45.0, 90.0), (50.0, 250.0),
        (5.0, 30.0), (45.0, 90.0), (50.0, 250.0),
        (5.0, 30.0), (45.0, 90.0), (50.0, 250.0),
        (10.0, 60.0), (90.0, 240.0), (100.0, 500.0))
      val actualSearchSpace = 
        tpService.activitySearchSpace(ActivityType.Run, validUserAttributes2)
      assert(actualSearchSpace == expectedSearchSpace)
    }

    "generate an initial heuristic cycling training plan" in {
      val actualTrainingPlan = seededService.generateInitialTrainingPlan(
        validUserAttributes1, 
        ActivityType.Ride)
      val expectedTrainingPlan = List(
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Ride,
          distance = Some(24.0),
          time = Some(46.794518484711645),
          elevation = Some(135.0620287074318),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan.head.createdAt),
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Ride,
          distance = Some(43.0),
          time = Some(96.5804493281509),
          elevation = Some(374.8305104770327),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan(1).createdAt))
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID) == expected)
      }
    }

    "generate an initial heuristic running training plan" in {
      val actualTrainingPlan = seededService.generateInitialTrainingPlan(
        validUserAttributes1, 
        ActivityType.Run)
      val expectedTrainingPlan = List(
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Run,
          distance = Some(6.5),
          time = Some(32.2015755844404),
          elevation = Some(26.235775755239754),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan.head.createdAt),
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Run,
          distance = Some(12.0),
          time = Some(64.91370061126626),
          elevation = Some(65.36443891159098),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan(1).createdAt))
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID) == expected)
      }
    }

    "generate an initial heuristic training plan with full user defined activity" in {
      val actualTrainingPlan = seededService.generateInitialTrainingPlan(
        validUserAttributes1, 
        ActivityType.Run,
        List(validUserDefinedActivity1))
      val expectedTrainingPlan = List(
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Run,
          distance = Some(12.0),
          time = Some(63.76618348131023),
          elevation = Some(62.96542592944798),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan.head.createdAt),
        validUserDefinedActivity1.copy(
          distance = None, 
          time = None, 
          elevation = None,
          createdAt = actualTrainingPlan(1).createdAt))
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID) == expected)
      }
    }

    "generate an initial heuristic training plan with user defined activity and one field missing" in {
      val actualTrainingPlan = seededService.generateInitialTrainingPlan(
        validUserAttributes1, 
        ActivityType.Run,
        List(validUserDefinedActivity2))
      val expectedTrainingPlan = List(
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Run,
          distance = Some(11.5),
          time = Some(62.3943875546833),
          elevation = Some(65.82446332057532),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan.head.createdAt),
        validUserDefinedActivity2.copy(
          distance = Some(6.0), 
          time = None, 
          elevation = None,
          createdAt = actualTrainingPlan(1).createdAt))
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID) == expected)
      }
    }

    "generate an initial heuristic training plan with user defined activity and two fields missing" in {
      val actualTrainingPlan = seededService.generateInitialTrainingPlan(
        validUserAttributes1, 
        ActivityType.Run,
        List(validUserDefinedActivity3))
      val expectedTrainingPlan = List(
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = UUID.randomUUID,
          ActivityType.Run,
          distance = Some(12.0),
          time = Some(65.85739029334877),
          elevation = Some(64.97769324484473),
          activityID = None,
          calendarEventID = None,
          createdAt = actualTrainingPlan.head.createdAt),
        validUserDefinedActivity3.copy(
          distance = Some(6.0), 
          time = None, 
          elevation = Some(31.915984345562592),
          createdAt = actualTrainingPlan(1).createdAt))
      expectedTrainingPlan.zip(actualTrainingPlan).foreach{
        case (expected, actual) =>
          assert(actual.copy(
            id = expected.id,
            userID = expected.userID) == expected)
      }
    }

    "generate short run" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Run)(ActivityLength.Short)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Run,
        distance = Some(1.0),
        time = Some(30.832564316445396),
        elevation = Some(30.889223500814655),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate average run" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Run)(ActivityLength.Average)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Run,
        distance = Some(1.0),
        time = Some(65.19905200105708),
        elevation = Some(67.97715791850273),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate long run" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Run)(ActivityLength.Long)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Run,
        distance = Some(1.0),
        time = Some(121.97267612377465),
        elevation = Some(116.72693433954399),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate short user defined run" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Run)(
        ActivityLength.Short)(
        Some(validUserDefinedActivity1))
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate average user defined run" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Run)(
        ActivityLength.Average)(
        Some(validUserDefinedActivity1))
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate long user defined run" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Run)(
        ActivityLength.Long)(
        Some(validUserDefinedActivity1))
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate short ride" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Ride)(ActivityLength.Short)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Ride,
        distance = Some(1.0),
        time = Some(45.91848870068552),
        elevation = Some(133.5848734861413),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate average ride" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Ride)(ActivityLength.Average)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Ride,
        distance = Some(1.0),
        time = Some(88.76510458322873),
        elevation = Some(373.0999200990922),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate long ride" in {
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(ActivityType.Ride)(ActivityLength.Long)(None)
      val expectedActivity = TrainingPlanActivity(
        id = UUID.randomUUID,
        userID = UUID.randomUUID,
        ActivityType.Ride,
        distance = Some(1.0),
        time = Some(184.29224542125647),
        elevation = Some(655.3201738070724),
        activityID = None,
        calendarEventID = None,
        createdAt = actualActivity.createdAt)
      assert(actualActivity.copy(
        id = expectedActivity.id,
        userID = expectedActivity.userID) == expectedActivity)
    }

    "generate short user defined ride" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Ride)(
        ActivityLength.Short)(
        Some(validUserDefinedActivity1))
      assert(actualActivity == expectedActivity)
    }

    "generate average user defined ride" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Ride)(
        ActivityLength.Average)(
        Some(validUserDefinedActivity1))
      assert(actualActivity == expectedActivity)
    }

    "generate long user defined ride" in {
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = seededService.generateActivity(
        validUserAttributes1)(
        ActivityType.Ride)(
        ActivityLength.Long)(
        Some(validUserDefinedActivity1))
      assert(actualActivity == expectedActivity)
    }

    "invert a full user defined activity" in {
      val (distance, time, elevation) = (1.0, 1.0, 1.0)
      val expectedActivity = validUserDefinedActivity1.copy(
        distance = None, time = None, elevation = None)
      val actualActivity = tpService.invertUserDefinedActivity(
        validUserAttributes1, ActivityType.Run, validUserDefinedActivity1,
        distance, time, elevation)
      assert(actualActivity == expectedActivity)
    }

    "invert a user defined activity with one field missing" in {
      val (distance, time, elevation) = (6.0, 1.0, 1.0)
      val expectedActivity = validUserDefinedActivity2.copy(
        distance = Some(distance), time = None, elevation = None)
      val actualActivity = tpService.invertUserDefinedActivity(
        validUserAttributes1, ActivityType.Run, validUserDefinedActivity2,
        distance, time, elevation)
      assert(actualActivity == expectedActivity)
    }

    "invert a full user defined activity with two fields missing" in {
      val (distance, time, elevation) = (6.0, 1.0, 1.0)
      val expectedActivity = validUserDefinedActivity3.copy(
        distance = Some(distance), time = None, elevation = Some(elevation))
      val actualActivity = tpService.invertUserDefinedActivity(
        validUserAttributes1, ActivityType.Run, validUserDefinedActivity3,
        distance, time, elevation)
      assert(actualActivity == expectedActivity)
    }

    "find the appropriate distance for a run" in {
      val sampleActivity = 
        validRunningTrainingPlan.head.copy(distance = Some(1.0))
      val expectedActivity = 
        validRunningTrainingPlan.head.copy(distance = Some(6.0))
      val actualActivity = tpService.findAppropriateDistanceForActivity(
        validUserAttributes1, ActivityType.Run)(sampleActivity)
      assert(actualActivity == expectedActivity)
    }

    "find the appropriate distance for a ride" in {
      val sampleActivity = 
        validCyclingTrainingPlan.head.copy(distance = Some(1.0))
      val expectedActivity = 
        validCyclingTrainingPlan.head.copy(distance = Some(21.0))
      val actualActivity = tpService.findAppropriateDistanceForActivity(
        validUserAttributes1, ActivityType.Ride)(sampleActivity)
      assert(actualActivity == expectedActivity)
    }
  }
}
