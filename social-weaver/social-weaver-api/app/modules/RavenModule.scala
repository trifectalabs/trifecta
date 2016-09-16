package modules

import com.trifectalabs.raven.v0.Client
import com.trifectalabs.raven.v0._

trait RavenModule extends ConfigModule {
  lazy val ravenUri = config.getString("services.raven.uri")
  lazy val ravenPort = config.getString("services.raven.port")

  lazy val trainingPlanClient: TrainingPlanActivities =
    new Client(s"$ravenUri:$ravenPort").trainingPlanActivities
}
