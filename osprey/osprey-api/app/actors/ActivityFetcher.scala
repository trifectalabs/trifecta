package actors

import akka.actor.Actor
import com.trifectalabs.osprey.v0.models._
import services.ActivityFetcherService
import modules._
import org.joda.time.DateTime
import play.api.Logger._
import play.api.Play.current
import play.api.cache.Cache
import resources._
import tables._
import java.util.UUID

import scala.util.{Failure, Success, Try}

class ActivityFetcher(service: ActivityFetcherService)
  extends Actor
  with DAOModule
  with ScravaModule {
    val modelRemapper = new ModelRemapper

    override def receive: Receive = {
      case tokenTuple: (UUID, String) => {
        val userID = tokenTuple._1
        val token = tokenTuple._2
        val currentEpoch = new DateTime().getMillis/1000
        val lastFetch = currentEpoch - 86400
        val streams = "time,latlng,distance,altitude,velocity_smooth,heartrate,cadence,watts_calc,temp,moving,grade_smooth"
        val scravaClient = scravaClientFactory.instance(token)


        scravaClient.listAthleteActivities(before = Some(currentEpoch.toInt), after = Some(lastFetch.toInt)) map { activity =>
          if (ActivityType.all.map(_.toString).toList contains activity.`type`) {
            Try {
              modelRemapper.scravaActivityToSlickActivity(activity, userID)
              } match {
                case Success((slickActivity, slickPerformance)) => {
                  // Save activity to database
                  PerformanceInfoTable.add(slickPerformance)
                  val id = slickPerformance.id
                  SingleActivitiesTable.add(slickActivity)
                  val actID = slickActivity.id
                  // Fetch all streams for activity
                  if (activity.device_watts.getOrElse(false) == true)
                    scravaClient.retrieveActivityStream(activity.id.toString) foreach (str => StreamTable.add(StreamPattern.matchStream(str, actID)))
                  else 
                    scravaClient.retrieveActivityStream(activity.id.toString, Some(streams)) foreach (str => StreamTable.add(StreamPattern.matchStream(str, actID)))
                }
              }
          }
          Cache.remove("tokens")
        }
      }
    }
  }
