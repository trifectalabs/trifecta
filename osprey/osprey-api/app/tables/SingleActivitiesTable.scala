package tables 

import com.websudos.phantom.dsl._
import com.trifectalabs.osprey.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future
import play.api.libs.json.Json
import scala.language.implicitConversions

class SingleActivitiesTable 
  extends CassandraTable[SingleActivitiesTable, SingleActivity] {
  override lazy val tableName = "single_activities"

  implicit def activityType(str: String): ActivityType = ActivityType(str)
  implicit def externalSource(str: String): ExternalSource = ExternalSource(str)
  implicit def flagsType(str: String): Seq[ActivityFlag] = {
    str.split(",") map(a => ActivityFlag(a))
  }

  object activity_id extends UUIDColumn(this) with PrimaryKey[UUID]
  object user_id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this) 
  object activity_type extends StringColumn(this) 
  object start_time extends DateTimeColumn(this)
  object start_time_local extends DateTimeColumn(this)
  object timezone extends StringColumn(this)
  object start_lat_lng extends OptionalStringColumn(this)
  object end_lat_lng extends OptionalStringColumn(this)
  object location_city extends OptionalStringColumn(this)
  object location_province extends OptionalStringColumn(this)
  object location_country extends OptionalStringColumn(this)
  object flags extends JsonListColumn[SingleActivitiesTable, SingleActivity, ActivityFlag](this) {
    override def fromJson(obj: String): ActivityFlag = {
      ActivityFlag(Json.toJson(obj).as[String])
    }

    override def toJson(obj: ActivityFlag): String = {
      Json.toJson(obj.toString).toString
    }
  }
  object description extends OptionalStringColumn(this)
  object performance_info_id extends UUIDColumn(this)
  object external_source extends StringColumn(this)
  object external_id extends StringColumn(this)

  def fromRow(row: Row): SingleActivity = {
    SingleActivity(
      activity_id(row), user_id(row), name(row), activity_type(row),
      start_time(row), start_time_local(row), timezone(row), start_lat_lng(row),
      end_lat_lng(row), location_city(row), location_province(row), location_country(row),
      flags(row), description(row), performance_info_id(row), 
      external_source(row), external_id(row))
  }
}

object SingleActivitiesTable extends SingleActivitiesTable with CassandraConnector with DAOModule {
  def findAllByUserID(userID: UUID): Future[Seq[SingleActivity]] = {
    select.where(_.user_id eqs userID).fetch()
  }

  def findByID(activityID: UUID): Future[Option[SingleActivity]] = {
    select.where(_.activity_id eqs activityID).one()
  }

  def add(sa: SingleActivity): Future[ResultSet] = {
    insert
      .value(_.activity_id, sa.id)
      .value(_.user_id, sa.userID)
      .value(_.name, sa.name)
      .value(_.activity_type, sa.activityType.toString)
      .value(_.start_time, sa.startTime)
      .value(_.start_time_local, sa.startTimeLocal)
      .value(_.timezone, sa.timezone)
      .value(_.start_lat_lng, sa.startLatLng)
      .value(_.end_lat_lng, sa.endLatLng)
      .value(_.location_city, sa.locationCity)
      .value(_.location_province, sa.locationProvince)
      .value(_.location_country, sa.locationCountry)
      .value(_.flags, sa.flags.toList)
      .value(_.description, sa.description)
      .value(_.performance_info_id, sa.performanceInfoID)
      .value(_.external_source, sa.externalSource.toString)
      .value(_.external_id, sa.externalID)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def remove(sa: SingleActivity): Future[ResultSet] = {
    delete.where(_.activity_id eqs sa.id)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
