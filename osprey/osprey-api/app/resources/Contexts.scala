package resources

import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.ExecutionContext

object Contexts {
  implicit val activityFetcherContext: ExecutionContext = Akka.system.dispatchers.lookup("activity-fetcher")
}
