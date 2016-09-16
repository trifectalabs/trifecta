package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.trifectalabs.raven.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future

class ActivityZonesTable extends CassandraTable[ActivityZonesTable, ActivityZones] {
  override lazy val tableName = "activity_zones"
  implicit def zoneType(str: String): ZoneType = ZoneType(str)

  object activity_id extends UUIDColumn(this) with PartitionKey[UUID]
  object zone_type extends StringColumn(this) with PrimaryKey[String] 
  object zone_one extends IntColumn(this) 
  object zone_two extends IntColumn(this) 
  object zone_three extends IntColumn(this)
  object zone_four extends IntColumn(this)
  object zone_five extends IntColumn(this)

  def fromRow(row: Row): ActivityZones = {
    ActivityZones(
      activity_id(row), zone_type(row), zone_one(row), zone_two(row),
      zone_three(row), zone_four(row), zone_five(row))
  }
}

object ActivityZonesTable extends ActivityZonesTable with CassandraConnector with DAOModule {
  def findByID(id: UUID): Future[List[ActivityZones]] = {
    select.where(_.activity_id eqs id).fetch().map(_.toList)
  }

  def add(zones: ActivityZones): Future[ActivityZones] = {
    insert
      .value(_.activity_id, zones.activityID)
      .value(_.zone_type, zones.zoneType.toString)
      .value(_.zone_one, zones.zoneOne)
      .value(_.zone_two, zones.zoneTwo)
      .value(_.zone_three, zones.zoneThree)
      .value(_.zone_four, zones.zoneFour)
      .value(_.zone_five, zones.zoneFive)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future() map { a =>
        zones
      }
  }

  def remove(zones: ActivityZones): Future[ResultSet] = {
    delete.where(_.activity_id eqs zones.activityID)
      .and(_.zone_type eqs zones.zoneType.toString)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
