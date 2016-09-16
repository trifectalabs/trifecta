package tables 

import java.util.Date
import com.websudos.phantom.dsl._
import com.trifectalabs.osprey.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future

class UserPhysicalAttributesTable extends CassandraTable[UserPhysicalAttributesTable, UserPhysicalAttributes] {
  override lazy val tableName = "user_physical_attributes"

  object user_id extends UUIDColumn(this) with PartitionKey[UUID]
  object height extends DoubleColumn(this) 
  object weight extends DoubleColumn(this) 
  object waist extends OptionalDoubleColumn(this) 
  object bmi extends OptionalDoubleColumn(this) 

  def fromRow(row: Row): UserPhysicalAttributes = {
    UserPhysicalAttributes(
      user_id(row), height(row), weight(row), waist(row),
      bmi(row))
  }
}

object UserPhysicalAttributesTable
  extends UserPhysicalAttributesTable
  with CassandraConnector with DAOModule {
    def findActiveByUserID(userID: UUID): Future[Option[UserPhysicalAttributes]] = {
      select.where(_.user_id eqs userID).one()
    }

    def add(attr: UserPhysicalAttributes): Future[ResultSet] = {
      insert
        .value(_.user_id, attr.userID)
        .value(_.height, attr.height)
        .value(_.weight, attr.weight)
        .value(_.waist, attr.waist)
        .value(_.bmi, attr.bmi)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }

    def remove(attr: UserPhysicalAttributes): Future[ResultSet] = {
      delete.where(_.user_id eqs attr.userID)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
  }
