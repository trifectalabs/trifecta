package resources 

import tables._
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.dsl._
import scala.concurrent.Future
import scala.language.postfixOps

object SchemaGenerator extends CassandraConnector {
  def generateAll = {
    Await.result(ActivityZonesTable.create.ifNotExists().future(), 5000 millis)
    Await.result(TrainingPlanActivitiesTable.create.ifNotExists().future(), 5000 millis)
    Await.result(TrainingZonesTable.create.ifNotExists().future(), 5000 millis)
  }
}
