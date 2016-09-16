package resources 

import com.trifectalabs.osprey.v0.models._
import org.joda.time.DateTime
import java.util.UUID

class ModelRemapper() {
  def scravaActivityToSlickActivity(activity: kiambogo.scrava.models.PersonalActivitySummary, userID: UUID): (SingleActivity, PerformanceInfo) = {
    val tFlags = if (activity.trainer) List(ActivityFlag.Trainer) else List()
    val cFlags = if (activity.commute) ActivityFlag.Commute :: tFlags else tFlags
    val mFlags = if (activity.manual) ActivityFlag.Manual :: cFlags else cFlags
    val fFlags = if (activity.flagged) ActivityFlag.Flagged :: mFlags else mFlags
    val pFlags = if (activity.device_watts.getOrElse(false)) ActivityFlag.PowerMeter :: mFlags else mFlags
    val allFlags = if (activity.`private`) ActivityFlag.Hidden :: pFlags else pFlags
    val perfID = UUID.randomUUID
    (SingleActivity(
      UUID.randomUUID(),
      userID,
      activity.name,
      ActivityType(activity.`type`),
      DateTime.parse(activity.start_date),
      DateTime.parse(activity.start_date_local),
      activity.timezone,
      Some(activity.start_latlng mkString (",")),
      Some(activity.end_latlng.getOrElse(List[Float](0,0)) mkString (",")),
      activity.location_city,
      activity.location_state,
      activity.location_country,
      allFlags,
      Some("description"),
      perfID,
      ExternalSource.Strava,
      activity.id.toString),
    PerformanceInfo(
      perfID,
      activity.distance.toDouble,
      activity.moving_time,
      activity.elapsed_time,
      activity.total_elevation_gain.toDouble,
      Some(activity.average_speed.toDouble),
      Some(activity.max_speed.toDouble),
      activity.average_watts map (_.toDouble),
      activity.average_cadence map (_.toDouble),
      activity.average_temp,
      activity.kilojoules map (_.toDouble),
      activity.average_heartrate map (_.toDouble),
      activity.max_heartrate map (_.toDouble)
    ))
  }
}
