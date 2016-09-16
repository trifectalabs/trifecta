package actors

import akka.actor.{Actor, ActorRef}
import services.ActivityFetcherService
import org.joda.time.DateTime
import play.api.Logger._

class QueueRunner(service: ActivityFetcherService, fetcherActor: ActorRef) extends Actor {
  override def receive: Receive = {
    case _ => {
      info("Fetching Activities - " + new DateTime())
      val tokens = service.fetchTokensFromQueue()
      tokens map { token =>
        if (token != "")
          fetcherActor ! token
      }
    }
  }
}
