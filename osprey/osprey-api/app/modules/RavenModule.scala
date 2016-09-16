package modules

import com.trifectalabs.raven.v0.Client
import com.trifectalabs.raven.v0._

trait RavenModule extends ConfigModule {
  lazy val ravenUri = config.getString("services.raven.uri")
  lazy val ravenPort = config.getString("services.raven.port")

  lazy val activityZonesClient: ActivityZones = 
    new Client(s"$ravenUri:$ravenPort").activityZones
  lazy val trainingPlanClient: TrainingPlanActivities =
    new Client(s"$ravenUri:$ravenPort").trainingPlanActivities
  lazy val trainingZonesClient: TrainingZones =
    new Client(s"$ravenUri:$ravenPort").trainingZones
}
