package services

import com.trifectalabs.osprey.v0.models.{PerformanceInfo, SingleActivity}
import modules.DAOModule
import play.api.Logger._
import play.api.Play.current
import play.api.cache.Cache
import tables._
import scala.util.{Failure, Success, Try}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

class ActivityFetcherService extends DAOModule {
  def placeTokensInQueue(): Unit = {
    def storeTokens: Future[Unit] = UserTable.getAll.map { allUsers =>
      val tokens = allUsers.filter(_.stravaToken != None).map(_.stravaToken)
      Cache.set("tokens", tokens)
    } 
  }

  def fetchTokensFromQueue(): List[(Int, String)] = {
    Cache.get("tokens") match {
      case Some(tokens) => {
        tokens.asInstanceOf[List[(Int, String)]]
      }
      case None => {
        debug("Could not retrieve any tokens from the queue!")
        List[(Int, String)]()
      }
    }
  }

  def saveActivity(activity: SingleActivity, performance: PerformanceInfo): Future[Boolean] = {
    SingleActivitiesTable.findByID(activity.id) map { optAct => optAct match {
      case Some(act) => true
      case None =>
        Try {
          PerformanceInfoTable.add(performance)
          SingleActivitiesTable.add(activity)
          } match {
            case Success(a) =>
              true
            case Failure(e) =>
              error("Error saving activity from actor: " + e)
              false
          }
    }
    }
  }
}
