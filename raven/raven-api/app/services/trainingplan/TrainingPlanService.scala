package services.trainingplan

import com.trifectalabs.myriad.pso.{TerminationCriteria, PSOConfiguration,
  PSOExecutor, PSOSystemFactory}
import models.{UserTraits, ParameterSearchSpace, SearchSpace,
  ActivitySearchSpace}
import com.trifectalabs.osprey.v0.models.ActivityType
import com.trifectalabs.raven.v0.models.{ActivityLength, TrainingPlanActivity}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.joda.time.DateTime
import java.util.UUID

class TrainingPlanService(randomSeed: Long = System.currentTimeMillis) {
  val r = new scala.util.Random(randomSeed)
  // Build a training plan through PSO of a heuristic initial plan
  def buildTrainingPlan(
    traits: UserTraits, 
    activityType: ActivityType,
    preDefinedActivities: List[TrainingPlanActivity],
    partiallyDefinedActivities: List[TrainingPlanActivity]
  ): Future[List[TrainingPlanActivity]] = {
    val solutions = Seq.fill(16)(TrainingPlanUtil.flattenTrainingPlan(
        generateInitialTrainingPlan(
          traits, 
          activityType, 
          partiallyDefinedActivities))).toList
    val conf = PSOConfiguration(
      objectiveFunction = 
        TrainingPlanUtil.objFunc(
          traits, 
          activityType, 
          preDefinedActivities,
          partiallyDefinedActivities),
      initialSolutions = solutions,
      searchSpace = Some(activitySearchSpace(activityType, traits)))
    val psoSystemFactory = new PSOSystemFactory(conf)
    val pso = psoSystemFactory.build()
    val psoJob = new PSOExecutor(pso)
    psoJob.run map { result =>
      val psoPlan = result.finalValue match {
        case Left(value) => value
        case Right(value) => List()
      }
      TrainingPlanUtil.interpretTrainingPlan(
        traits.userID, activityType)(partiallyDefinedActivities, psoPlan)
    }
  }
  // Get the optimization search space for the activity type
  def activitySearchSpace(
    activityType: ActivityType, 
    t: UserTraits
  ): List[(Double, Double)] = {
    val (ss, activities, variance) = activityType match {
      case ActivityType.Run =>
        val searchSpace = ActivitySearchSpace(
          short = SearchSpace(
            distance = ParameterSearchSpace(1.0, 15.0),
            time = ParameterSearchSpace(15.0, 45.0),
            elevation = ParameterSearchSpace(0.0, 75.0)),
          avg = SearchSpace(
            distance = ParameterSearchSpace(5.0, 30.0),
            time = ParameterSearchSpace(45.0, 90.0),
            elevation = ParameterSearchSpace(50.0, 250.0)),
          long = SearchSpace(
            distance = ParameterSearchSpace(10.0, 60.0),
            time = ParameterSearchSpace(90.0, 240.0),
            elevation = ParameterSearchSpace(100.0, 500.0)))
        (searchSpace, t.runningActivities, t.runningVariance)
      case ActivityType.Ride =>
        val searchSpace = ActivitySearchSpace(
          short = SearchSpace(
            distance = ParameterSearchSpace(5.0, 50.0),
            time = ParameterSearchSpace(30.0, 60.0),
            elevation = ParameterSearchSpace(50.0, 450.0)),
          avg = SearchSpace(
            distance = ParameterSearchSpace(10.0, 100.0),
            time = ParameterSearchSpace(60.0, 120.0),
            elevation = ParameterSearchSpace(250.0, 1000.0)),
          long = SearchSpace(
            distance = ParameterSearchSpace(20.0, 180.0),
            time = ParameterSearchSpace(120.0, 360.0),
            elevation = ParameterSearchSpace(500.0, 2000.0)))
        (searchSpace, t.cyclingActivities, t.cyclingVariance)
      case _ => throw new RuntimeException("Unsupported activity type")
    }
    val counts = TrainingPlanUtil.varianceFractionToCount(variance, activities)
    (Seq.fill(counts(ActivityLength.Short))(List(
      (ss.short.distance.lower, ss.short.distance.upper),
      (ss.short.time.lower, ss.short.time.upper),
      (ss.short.elevation.lower, ss.short.elevation.upper))) ++
      Seq.fill(counts(ActivityLength.Average))(List(
        (ss.avg.distance.lower, ss.avg.distance.upper),
        (ss.avg.time.lower, ss.avg.time.upper),
        (ss.avg.elevation.lower, ss.avg.elevation.upper))) ++
      Seq.fill(counts(ActivityLength.Long))(List(
        (ss.long.distance.lower, ss.long.distance.upper),
        (ss.long.time.lower, ss.long.time.upper),
        (ss.long.elevation.lower, ss.long.elevation.upper)))).toList.flatten
  }
  // Heuristic training plan initializer
  //
  // Based on athlete's preferred variance make a spread of activities
  // with random noise for both time and elevation. Then find an appropriate
  // matching distance for the athlete's fitness level.
  def generateInitialTrainingPlan(
    t: UserTraits, 
    activityType: ActivityType,
    partiallyDefinedActivities: List[TrainingPlanActivity] = Nil
  ): List[TrainingPlanActivity] = {
    val (gen, counts, activityLengthRange) = activityType match {
      case ActivityType.Run =>
        (generateActivity(t)(ActivityType.Run) _,
          TrainingPlanUtil.varianceFractionToCount(
            t.runningVariance, t.runningActivities),
          t.runningActivityLengthRange)
      case ActivityType.Ride =>
        (generateActivity(t)(ActivityType.Ride) _,
          TrainingPlanUtil.varianceFractionToCount(
            t.cyclingVariance, t.cyclingActivities),
          t.cyclingActivityLengthRange)
      case _ => throw new RuntimeException("Unsupported activity type")
    }
    val genShort = gen(ActivityLength.Short)
    val genAvg = gen(ActivityLength.Average)
    val genLong = gen(ActivityLength.Long)
    val shortParDef = partiallyDefinedActivities filter { a =>
      val range = activityLengthRange("Short")
      val time = a.time.getOrElse(0.0)
      time >= range.lowRange && time < range.highRange
    }
    val avgParDef = partiallyDefinedActivities filter { a =>
      val range = activityLengthRange("Average")
      val time = a.time.getOrElse(0.0)
      time >= range.lowRange && time < range.highRange
    }
    val longParDef = partiallyDefinedActivities filter { a =>
      val range = activityLengthRange("Long")
      val time = a.time.getOrElse(0.0)
      time >= range.lowRange
    }
    val shortActivities = Seq.fill(counts(ActivityLength.Short) - 
      shortParDef.length)(genShort(None)).toList
    val averageActivities = Seq.fill(counts(ActivityLength.Average) -
      avgParDef.length)(genAvg(None)).toList
    val longActivities = Seq.fill(counts(ActivityLength.Long) -
      longParDef.length)(genLong(None)).toList
    val getDistance = findAppropriateDistanceForActivity(t, activityType) _
    shortActivities.map(getDistance) ++
      averageActivities.map(getDistance) ++
      longActivities.map(getDistance) ++
      shortParDef.map(a => genShort(Some(a))) ++
      avgParDef.map(a => genAvg(Some(a))) ++
      longParDef.map(a => genLong(Some(a)))
  }
  // generate a single activity for a training plan
  //
  // Provide an optional user defined activity. If this activity is provided the
  // returned activity will return an activity with the distance/time/elevation
  // fields that need to be optimized while the rest are undefined
  def generateActivity
    (t: UserTraits)
    (activityType: ActivityType)
    (activityLength: ActivityLength)
    (activity: Option[TrainingPlanActivity] = None)
  : TrainingPlanActivity = {
    val (distance, time, elevation) = activityType match {
      case ActivityType.Run => activityLength match {
        case ActivityLength.Short => (
          1.0,
          30.0 + r.nextGaussian() * math.sqrt(5),
          30.0 + r.nextGaussian() * math.sqrt(5))
        case ActivityLength.Average => (
          1.0,
          65 + r.nextGaussian() * math.sqrt(10),
          65 + r.nextGaussian() * math.sqrt(10))
        case ActivityLength.Long => (
          1.0,
          120 + r.nextGaussian() * math.sqrt(20),
          120 + r.nextGaussian() * math.sqrt(20))
        case _ => throw new RuntimeException("Unsupported activity length")
      }
      case ActivityType.Ride => activityLength match {
        case ActivityLength.Short => (
          1.0,
          45 + r.nextGaussian() * math.sqrt(5),
          140 + r.nextGaussian() * math.sqrt(30))
        case ActivityLength.Average => (
          1.0,
          90 + r.nextGaussian() * math.sqrt(10),
          370 + r.nextGaussian() * math.sqrt(40))
        case ActivityLength.Long => (
          1.0,
          180 + r.nextGaussian() * math.sqrt(20),
          650 + r.nextGaussian() * math.sqrt(50))
        case _ => throw new RuntimeException("Unsupported activity length")
      }
      case _ => throw new RuntimeException("Unsupported activity type")
    }
    activity match {
      case None => 
        TrainingPlanActivity(
          id = UUID.randomUUID,
          userID = t.userID,
          activityType = activityType,
          distance = Some(distance),
          time = Some(time),
          elevation = Some(elevation),
          createdAt = new DateTime)
      case Some(a) => 
        invertUserDefinedActivity(
          t, 
          activityType, 
          a, 
          distance, 
          time, 
          elevation)
    }
  }
  // Return activity with distance/time/elevation values where they were
  // previously undefined and undefined values where there previously were 
  // distance/time/elevation values
  def invertUserDefinedActivity(
    traits: UserTraits, 
    activityType: ActivityType, 
    a: TrainingPlanActivity,
    distance: Double, 
    time: Double, 
    elevation: Double
  ): TrainingPlanActivity = {
    val d = a.distance match {
      case None =>
        val act = a.copy(
          distance = Some(distance), 
          time = Some(a.time.getOrElse(time)), 
          elevation = Some(a.elevation.getOrElse(elevation)))
        findAppropriateDistanceForActivity(traits, activityType)(act).distance
      case _ => None
    }
    val t = a.time match {
      case None => Some(time)
      case _ => None
    }
    val e = a.elevation match {
      case None => Some(elevation)
      case _ => None
    }
    a.copy(distance = d, time = t, elevation = e)
  }
  // Increment the distance of an activity until it is around the athlete's 
  // fitness level
  def findAppropriateDistanceForActivity(
    t: UserTraits, 
    activityType: ActivityType)
    (a: TrainingPlanActivity)
  : TrainingPlanActivity = {
    val (level, fitnessLevel) = activityType match {
      case ActivityType.Run => 
        (TrainingPlanUtil.runningLevel(t.weight)(a), t.runningLevel)
      case ActivityType.Ride => 
        (TrainingPlanUtil.cyclingLevel(
          t.weight, 
          t.bikeRollingResistance, 
          t.bikeDrag)(a), t.cyclingLevel)
      case _ => throw new RuntimeException("Unsupported activity type")
    }
    if (level < fitnessLevel) {
      findAppropriateDistanceForActivity(t, activityType)(
        a.copy(distance = Some(TrainingPlanUtil.get(a.distance) + 0.5)))
    } else {
      a
    }
  }
}

