package services

import modules._
import com.trifectalabs.osprey.v0.models._
import com.trifectalabs.raven.v0.models._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.{Try, Success, Failure}
import java.util.UUID
import tables._

class ActivityZonesService extends OspreyModule {
  def saveAllZonesForActivity(zones: List[ActivityZones]): Future[List[ActivityZones]] = {
    Future.sequence(zones.map(zone => ActivityZonesTable.add(zone)))
  }

  def toInt(value: String): Int = {
    Try(value.toInt) match {
      case Success(v) => v
      case Failure(e) => 0
    }
  }

  def toDouble(value: String): Double = {
    Try(value.toDouble) match {
      case Success(v) => v
      case Failure(e) => 0.0
    }
  }

  def getStream(
    streams: Seq[Stream],
    streamType: StreamType,
    fallbackType: Option[StreamType] = None
  ): Seq[Stream] = {
    val fallback = fallbackType.getOrElse(streamType)
    streams.filter(s => s.streamType == streamType) match {
      case Nil =>
        streams.filter(s => s.streamType == fallback) match {
          case Nil => Seq()
          case s => s
        }
      case s => s
    }
  }

  //create all the relevant activity zones for an activity
  //bike => hr if available and power
  //run => hr if available and pace
  def getZonesFromActivity(
    activity: SingleActivity
  ): Future[List[ActivityZones]] = {
    performanceInfoClient.getById(activity.performanceInfoID)
      .flatMap { p =>
        p.averageHeartrate match {
          case None =>
            activity.activityType match {
              case ActivityType.Ride | ActivityType.VirtualRide =>
                for {
                  streams <- streamsClient.get(activity.id, Seq(
                    StreamType.Time, StreamType.Power, StreamType.PowerCalc))
                  timeStream = getStream(streams, StreamType.Time).head
                  length = timeStream.data.split(",").length
                  powerStream = getStream(streams, StreamType.Power,
                    Some(StreamType.PowerCalc))
                      .headOption
                      .getOrElse(timeStream.copy(
                        data = Seq.fill(length)(0).mkString(",")))
                      powerZones <- getPowerZonesFromActivity(
                        activity,
                        timeStream.data.split(",").toList.map(toInt),
                        powerStream.data.split(",").toList.map(toInt))
                } yield {
                  List(powerZones)
                }
              case ActivityType.Run =>
                for {
                  streams <- streamsClient.get(activity.id,
                    Seq(StreamType.Time, StreamType.Velocity))
                  timeStream = getStream(streams, StreamType.Time).head
                  length = timeStream.data.split(",").length
                  velocityStream =
                    getStream(streams, StreamType.Velocity).head
                  paceZones <- getPaceZonesFromActivity(
                    activity,
                    timeStream.data.split(",").toList.map(toInt),
                    velocityStream.data.split(",").toList.map(toDouble))
                } yield {
                  List(paceZones)
                }
              case _ => 
                throw new RuntimeException(
                  "getting zones for unsupported activity type " +
                  activity.activityType)
            }
          case _ =>
            activity.activityType match {
              case ActivityType.Ride | ActivityType.VirtualRide =>
                for {
                  streams <- streamsClient.get(activity.id, Seq(
                    StreamType.Time, StreamType.Heartrate, StreamType.Power,
                    StreamType.PowerCalc))

                  timeStream = getStream(streams, StreamType.Time).head
                  length = timeStream.data.split(",").length
                  hrStream = getStream(streams, StreamType.Heartrate).head
                  powerStream = getStream(streams, StreamType.Power,
                    Some(StreamType.PowerCalc))
                    .headOption
                    .getOrElse(timeStream.copy(
                      data = Seq.fill(length)(0).mkString(",")))

                    hrZones <- getHRZonesFromActivity(
                      activity,
                      timeStream.data.split(",").toList.map(toInt),
                      hrStream.data.split(",").toList.map(toInt))

                    powerZones <- getPowerZonesFromActivity(
                      activity,
                      timeStream.data.split(",").toList.map(toInt),
                      powerStream.data.split(",").toList.map(toInt))
                } yield {
                  List(hrZones, powerZones)
                }
              case ActivityType.Run =>
                for {
                  streams <- streamsClient.get(activity.id, Seq(
                    StreamType.Time, StreamType.Velocity, StreamType.Heartrate))
                  timeStream = getStream(streams, StreamType.Time).head
                  length = timeStream.data.split(",").length
                  hrStream = getStream(streams, StreamType.Heartrate).head
                  velocityStream =
                    getStream(streams, StreamType.Velocity).head
                  hrZones <- getHRZonesFromActivity(
                      activity,
                      timeStream.data.split(",").toList.map(toInt),
                      hrStream.data.split(",").toList.map(toInt))
                  paceZones <- getPaceZonesFromActivity(
                      activity,
                      timeStream.data.split(",").toList.map(toInt),
                      velocityStream.data.split(",").toList.map(toDouble))
                } yield {
                  List(hrZones, paceZones)
                }
              case _ =>
                throw new RuntimeException(
                  "getting zones for unsupported activity type " +
                  activity.activityType)
            }
        }
    }
  }

  def getHRZonesFromActivity(
    activity: SingleActivity,
    timeStream: List[Int],
    hrStream: List[Int]
  ): Future[ActivityZones] = {
    TrainingZonesTable.findByUserID(activity.userID).map { allZones =>
      val zones = allZones.filter(_.zoneType == ZoneType.Hr).head
      getZonesFromStream(zones, activity.id, timeStream, hrStream.map(_.toDouble))
    }
  }

  def getPowerZonesFromActivity(
    activity: SingleActivity,
    timeStream: List[Int],
    powerStream: List[Int]
  ): Future[ActivityZones] = {
    TrainingZonesTable.findByUserID(activity.userID).map{ allZones =>
      val zones = allZones.filter(_.zoneType == ZoneType.Power).head
      getZonesFromStream(zones, activity.id, timeStream,
        powerStream.map(_.toDouble))
    }
  }

  def getPaceZonesFromActivity(
    activity: SingleActivity,
    timeStream: List[Int],
    velocityStream: List[Double]
  ): Future[ActivityZones] = {
    TrainingZonesTable.findByUserID(activity.userID).map { allZones =>
      val zones = allZones.filter(_.zoneType == ZoneType.Pace).head
      getZonesFromStream(zones, activity.id, timeStream,
        velocityStream.map(a => (1/a) * (1000/60)))
    }
  }

  /* Build activity zones based on a time stream, data stream, and valid stream
   *
   * streamSplitByZone:
   * Parse the list of data points (stream) and group together points that are
   * similar (in the same training zone). If the next data point in the list is
   * in the same zone as the head of the aggregator then group it into the head.
   * If it is not in the same zone as the head of the aggregator start a new
   * group and add it to the head of the aggregator.
   *
   * streamTimeInZones:
   * For each group determin the length of time from the first to last point.
   *
   * timeInZones:
   * Group by zone number and sum times to dtermine total time in each zone.
   */
  def getZonesFromStream(
    zones: TrainingZones,
    activityID: UUID,
    time: List[Int],
    stream: List[Double]
  ): ActivityZones = {
    val streamSplitByZone = time.zip(stream)
      .foldLeft[List[List[(String, Int, Double)]]](Nil)({(agr, next) =>
        val zone = getZone(zones, next._2)
        agr match {
          case Nil => List(List((zone, next._1, next._2)))
          case _ =>
            if (agr.head.head._1 == zone) {
              ((zone, next._1, next._2)::agr.head)::agr.tail
            } else {
              List((zone, next._1, next._2))::agr
            }
        }
    })
    val streamTimeInZones = streamSplitByZone map { split =>
      split.length match {
        case 1 => (split.head._1, 1)
        case _ => (split.head._1, split.head._2 - split.last._2)
      }
    }
    val timeInZones = streamTimeInZones.groupBy(_._1).map{case (zone, times) =>
      (zone, times.unzip._2.sum)
    }
    ActivityZones(
      activityID = activityID,
      zoneType = zones.zoneType,
      zoneOne = timeInZones.getOrElse("z1", 0),
      zoneTwo = timeInZones.getOrElse("z2", 0),
      zoneThree = timeInZones.getOrElse("z3", 0),
      zoneFour = timeInZones.getOrElse("z4", 0),
      zoneFive = timeInZones.getOrElse("z5", 0)
    )
  }

  //determine zone of data point based on users training zones
  def getZone(zones: TrainingZones, data: Double): String = {
    zones.zoneType match {
      case ZoneType.Hr =>
        data match {
          case d
            if d >= zones.zoneFive.lower => "z5"
          case d
            if d >= zones.zoneFour.lower && d < zones.zoneFour.upper => "z4"
          case d
            if d >= zones.zoneThree.lower && d < zones.zoneThree.upper => "z3"
          case d
            if d >= zones.zoneTwo.lower && d < zones.zoneTwo.upper => "z2"
          case d
            if d < zones.zoneOne.upper => "z1"
        }
      case ZoneType.Power =>
        data match {
          case d
            if d >= zones.zoneFive.lower => "z5"
          case d
            if d >= zones.zoneFour.lower && d < zones.zoneFour.upper => "z4"
          case d
            if d >= zones.zoneThree.lower && d < zones.zoneThree.upper => "z3"
          case d
            if d >= zones.zoneTwo.lower && d < zones.zoneTwo.upper => "z2"
          case d
            if d < zones.zoneOne.upper => "z1"
        }
      case ZoneType.Pace =>
        data match {
          case d
            if d >= zones.zoneOne.lower => "z1"
          case d
            if d >= zones.zoneTwo.lower && d < zones.zoneTwo.upper => "z2"
          case d
            if d >= zones.zoneThree.lower && d < zones.zoneThree.upper => "z3"
          case d
            if d >= zones.zoneFour.lower && d < zones.zoneFour.upper => "z4"
          case d
            if d < zones.zoneFive.upper => "z5"
        }
      case _ => 
        throw new RuntimeException("getting zone for undefined zone type")
    }
  }
}

