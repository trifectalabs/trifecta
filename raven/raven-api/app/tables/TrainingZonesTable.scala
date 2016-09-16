package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.trifectalabs.raven.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future
import play.api.libs.json.Json

class TrainingZonesTable extends CassandraTable[TrainingZonesTable, TrainingZones] {
  override lazy val tableName = "training_zones"
  implicit def zoneType(str: String): ZoneType = ZoneType(str)
  implicit def trainingZone(str: String): TrainingZone = {
    val a = str.split(",")
    TrainingZone(a(0).toDouble, a(1).toDouble)
  }
  def tZToStr(tz: TrainingZone): String = s"${tz.lower},${tz.upper}"

  object user_id extends UUIDColumn(this) with PartitionKey[UUID]
  object zone_type extends StringColumn(this) with PrimaryKey[String] 
  object zone_one extends StringColumn(this) 
  object zone_two extends StringColumn(this) 
  object zone_three extends StringColumn(this) 
  object zone_four extends StringColumn(this) 
  object zone_five extends StringColumn(this) 
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): TrainingZones = {
    TrainingZones(
      user_id(row), zone_type(row), zone_one(row), zone_two(row),
      zone_three(row), zone_four(row), zone_five(row), created_at(row))
  }
}

object TrainingZonesTable extends TrainingZonesTable with CassandraConnector with DAOModule {
  def findByUserID(id: UUID): Future[List[TrainingZones]] = {
    select.where(_.user_id eqs id).fetch().map(_.toList)
  }

  def add(zones: TrainingZones): Future[ResultSet] = {
    insert
      .value(_.user_id, zones.userID)
      .value(_.zone_type, zones.zoneType.toString)
      .value(_.zone_one, tZToStr(zones.zoneOne))
      .value(_.zone_two, tZToStr(zones.zoneTwo))
      .value(_.zone_three, tZToStr(zones.zoneThree))
      .value(_.zone_four, tZToStr(zones.zoneFour))
      .value(_.zone_five, tZToStr(zones.zoneFive))
      .value(_.created_at, zones.createdAt)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def remove(zones: TrainingZones): Future[ResultSet] = {
    delete.where(_.user_id eqs zones.userID)
      .and(_.zone_type eqs zones.zoneType.toString)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
