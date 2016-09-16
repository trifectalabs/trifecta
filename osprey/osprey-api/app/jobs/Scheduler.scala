package jobs

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import play.api.Logger._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration.FiniteDuration

object Scheduler extends App {
  class ActivityScheduler(
    tokenQueuer: ActorRef,
    activityFetcher: ActorRef,
    queueRunner: ActorRef,
    cumulativeActivityGenerator: ActorRef,
    system: ActorSystem) {
      info("Starting Scheduler")
      system.scheduler.schedule(FiniteDuration(5, TimeUnit.SECONDS), FiniteDuration(1, TimeUnit.HOURS), tokenQueuer, "")
      system.scheduler.schedule(FiniteDuration(10, TimeUnit.SECONDS), FiniteDuration(1, TimeUnit.HOURS), queueRunner, "")
      system.scheduler.schedule(FiniteDuration(0, TimeUnit.SECONDS), FiniteDuration(1, TimeUnit.DAYS), cumulativeActivityGenerator, "")
    }
}
