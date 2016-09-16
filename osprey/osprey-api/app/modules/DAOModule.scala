package modules

import tables._

trait DAOModule extends ConfigModule {
  val users = UserTable
  val emailsToIDs = EmailToUserTable 
  val userActivityAttributes = UserActivityAttributesTable
  val userPhysicalAttributes = UserPhysicalAttributesTable
  val streams = StreamTable
  val performanceInfo = PerformanceInfoTable
  val singleActivities = SingleActivitiesTable
}
