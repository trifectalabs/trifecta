package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.trifectalabs.raven.v0.models._
import com.trifectalabs.osprey.v0.models.ActivityType
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future
import scala.util._

class TrainingPlanActivitiesTable extends CassandraTable[TrainingPlanActivitiesTable, TrainingPlanActivity] {
  override lazy val tableName = "training_plan_activities"
  implicit def activityType(str: String): ActivityType = ActivityType(str)

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object user_id extends UUIDColumn(this) with PrimaryKey[UUID] 
  object activity_type extends StringColumn(this) 
  object distance extends OptionalDoubleColumn(this) 
  object time extends OptionalDoubleColumn(this)
  object elevation extends OptionalDoubleColumn(this)
  object activity_id extends OptionalUUIDColumn(this)
  object calendar_event_id extends OptionalStringColumn(this)
  object polyline extends OptionalStringColumn(this)
  object scheduled_at extends OptionalDateTimeColumn(this)
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): TrainingPlanActivity = {
    TrainingPlanActivity(
      id(row), user_id(row), activity_type(row), distance(row),
      time(row), elevation(row), activity_id(row), calendar_event_id(row),
      polyline(row), scheduled_at(row), created_at(row))
  }
}

object TrainingPlanActivitiesTable extends TrainingPlanActivitiesTable with CassandraConnector with DAOModule {
  def findByID(id: UUID): Future[Option[TrainingPlanActivity]] = {
    select.where(_.id eqs id).one()
  }

  def findByUserID(userID: UUID): Future[List[TrainingPlanActivity]] = {
    select.where(_.user_id eqs userID).allowFiltering.fetch().map(_.toList)
  }

  def add(act: TrainingPlanActivity): Future[ResultSet] = {
    insert
      .value(_.id, act.id)
      .value(_.user_id, act.userID)
      .value(_.activity_type, act.activityType.toString)
      .value(_.distance, act.distance)
      .value(_.time, act.time)
      .value(_.elevation, act.elevation)
      .value(_.activity_id, act.activityID)
      .value(_.calendar_event_id, act.calendarEventID)
      .value(_.polyline, act.polyline)
      .value(_.scheduled_at, act.scheduledAt)
      .value(_.created_at, act.createdAt)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def amend(act: TrainingPlanActivity): Future[ResultSet] = {
    update
      .where(_.id eqs act.id)
      .and(_.user_id eqs act.userID)
      .modify(_.scheduled_at setTo act.scheduledAt)
      .and(_.polyline setTo act.polyline)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def remove(actID: UUID): Future[ResultSet] = {
    delete.where(_.id eqs actID)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
