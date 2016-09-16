package actors

import akka.actor.Actor
import services.ActivityFetcherService

class TokenQueuer(service: ActivityFetcherService) extends Actor {
  override def receive: Receive = {
    case _ => {
      service.placeTokensInQueue()
    }
  }
}
