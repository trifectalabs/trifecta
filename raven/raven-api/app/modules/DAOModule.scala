package modules

import tables._

trait DAOModule extends ConfigModule {
  val activityZones = ActivityZonesTable 
  val trainingPlanActivities = TrainingPlanActivitiesTable 
  val trainingZones = TrainingZonesTable 
}
