package modules

import com.trifectalabs.osprey.v0.Client
import com.trifectalabs.osprey.v0._

trait OspreyModule extends ConfigModule {
  lazy val ospreyUri = config.getString("services.osprey.uri")
  lazy val ospreyPort = config.getString("services.osprey.port")

  lazy val streamsClient: Streams = new Client(s"$ospreyUri:$ospreyPort").streams
  lazy val performanceInfoClient: PerformanceInfo = new Client(s"$ospreyUri:$ospreyPort").performanceInfo
  lazy val activitiesClient: SingleActivities = new Client(s"$ospreyUri:$ospreyPort").singleActivities
  lazy val userActivityAttributesClient: UserActivityAttributes = new Client(s"$ospreyUri:$ospreyPort").userActivityAttributes
  lazy val userPhysicalAttributesClient: UserPhysicalAttributes = new Client(s"$ospreyUri:$ospreyPort").userPhysicalAttributes
}
