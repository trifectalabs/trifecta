package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.trifectalabs.osprey.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future

class PerformanceInfoTable extends CassandraTable[PerformanceInfoTable, PerformanceInfo] {
  override lazy val tableName = "performance_info"

  object performance_info_id extends UUIDColumn(this) with PartitionKey[UUID]
  object distance extends DoubleColumn(this) 
  object moving_time extends IntColumn(this) 
  object elapsed_time extends IntColumn(this) 
  object total_elevation_gain extends DoubleColumn(this)
  object average_speed extends OptionalDoubleColumn(this)
  object max_speed extends OptionalDoubleColumn(this)
  object average_power extends OptionalDoubleColumn(this)
  object average_cadence extends OptionalDoubleColumn(this)
  object average_temp extends OptionalIntColumn(this)
  object kilojoules extends OptionalDoubleColumn(this)
  object average_heartrate extends OptionalDoubleColumn(this)
  object max_heartrate extends OptionalDoubleColumn(this)
  object calories extends OptionalDoubleColumn(this)

  def fromRow(row: Row): PerformanceInfo = {
    PerformanceInfo(
      performance_info_id(row), distance(row), moving_time(row), elapsed_time(row),
      total_elevation_gain(row), average_speed(row), max_speed(row), average_power(row),
      average_cadence(row), average_temp(row), kilojoules(row), average_heartrate(row),
      max_heartrate(row), calories(row))
  }
}

object PerformanceInfoTable extends PerformanceInfoTable with CassandraConnector with DAOModule {
  def findByID(id: UUID): Future[Option[PerformanceInfo]] = {
    select.where(_.performance_info_id eqs id).one()
  }

  def add(info: PerformanceInfo): Future[ResultSet] = {
    insert
      .value(_.performance_info_id, info.id)
      .value(_.distance, info.distance)
      .value(_.moving_time, info.movingTime)
      .value(_.elapsed_time, info.elapsedTime)
      .value(_.total_elevation_gain, info.totalElevationGain)
      .value(_.average_speed, info.averageSpeed)
      .value(_.max_speed, info.maxSpeed)
      .value(_.average_power, info.averagePower)
      .value(_.average_cadence, info.averageCadence)
      .value(_.average_temp, info.averageTemp)
      .value(_.kilojoules, info.kilojoules)
      .value(_.average_heartrate, info.averageHeartrate)
      .value(_.max_heartrate, info.maxHeartrate)
      .value(_.calories, info.calories)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def remove(info: PerformanceInfo): Future[ResultSet] = {
    delete.where(_.performance_info_id eqs info.id)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
