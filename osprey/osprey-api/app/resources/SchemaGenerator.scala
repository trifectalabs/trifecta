package resources 

import tables._
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.dsl._
import scala.concurrent.Future
import scala.language.postfixOps

object SchemaGenerator extends CassandraConnector {
  def generateAll = {
    Await.result(PerformanceInfoTable.create.ifNotExists().future(), 5000 millis)
    Await.result(StreamTable.create.ifNotExists().future(), 5000 millis)
    Await.result(SingleActivitiesTable.create.ifNotExists().future(), 5000 millis)
    Await.result(UserActivityAttributesTable.create.ifNotExists().future(), 5000 millis)
    Await.result(UserPhysicalAttributesTable.create.ifNotExists().future(), 5000 millis)
    Await.result(UserTable.create.ifNotExists().future(), 5000 millis)
    Await.result(EmailToUserTable.create.ifNotExists().future(), 5000 millis)
  }
}
