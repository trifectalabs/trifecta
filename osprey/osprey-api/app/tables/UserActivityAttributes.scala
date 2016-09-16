package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.websudos.phantom.CassandraTable
import com.trifectalabs.osprey.v0.models._
import resources.CassandraConnector
import scala.concurrent.Future
import scala.language.implicitConversions

class UserActivityAttributesTable extends CassandraTable[
UserActivityAttributesTable, UserActivityAttributes] {
  override lazy val tableName = "user_activity_attributes"
  implicit def activityType(str: String): ActivityType = ActivityType(str)
  implicit def activityVariance(str: String): ActivityVariance = {
    val a = str.split(",").map(_.toDouble) 
    ActivityVariance(a(0), a(1), a(2)) 
  }
  def varianceToStr(variance: ActivityVariance): String = {
    s"${variance.fractionShort},${variance.fractionAverage},${variance.fractionLong}"
  }
  implicit def activitySpecific(str: String): Map[String, Double] = {
    if (str == "") Map()
    else {
      str.trim.split(",").flatMap { e=>
        val a = e.split("->")
        Map(a(0)->a(1).toDouble)
      }.toMap
    }
  }
  implicit def activityLengthRange(str: String): Map[String, ActivityLengthRange] = {
    str.split(".").flatMap { e=>
      val a = e.split("->")
      val (low,high) = (a(1).split(",")(0).toInt,a(1).split(",")(1).toInt)
      Map(a(0)->ActivityLengthRange(low, high))
    }.toMap
  }

  object user_id extends UUIDColumn(this) with PartitionKey[UUID]
  object activity_type extends StringColumn(this) with PrimaryKey[String]
  object level extends DoubleColumn(this) 
  object days extends IntColumn(this) 
  object number_of_activities extends IntColumn(this) 
  object longest_distance extends DoubleColumn(this) 
  object activity_length_range extends StringColumn(this) 
  object variance extends StringColumn(this) 
  object activity_specific extends StringColumn(this) 
  object created_at extends DateTimeColumn(this) 

  def fromRow(row: Row): UserActivityAttributes = {
    UserActivityAttributes(
      user_id(row), activity_type(row), level(row), days(row),
      number_of_activities(row), longest_distance(row), activity_length_range(row),
      variance(row), activity_specific(row), created_at(row))
  }
}

object UserActivityAttributesTable extends UserActivityAttributesTable with CassandraConnector {
  def findActiveByUserIDAndActivityType(userID: UUID, activityType: ActivityType): Future[Option[UserActivityAttributes]] = {
    select.where(_.user_id eqs userID)
      .and(_.activity_type eqs activityType.toString)
      .one()
  }

  def findActiveByUserID(userID: UUID): Future[Seq[UserActivityAttributes]] = {
    select.where(_.user_id eqs userID).fetch()
  }

  def add(attr: UserActivityAttributes): Future[ResultSet] = {
    insert
      .value(_.user_id, attr.userID)
      .value(_.activity_type, attr.activityType.toString)
      .value(_.level, attr.level)
      .value(_.days, attr.days)
      .value(_.number_of_activities, attr.activities)
      .value(_.longest_distance, attr.longestDistance)
      .value(_.activity_length_range, attr.activityLengthRange.toString)
      .value(_.variance, varianceToStr(attr.variance))
      .value(_.activity_specific, attr.activitySpecific.mkString(","))
      .value(_.created_at, attr.createdAt)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }

  def remove(attr: UserActivityAttributes): Future[ResultSet] = {
    delete.where(_.user_id eqs attr.userID)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
