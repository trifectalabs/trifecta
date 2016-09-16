package tables 

import com.websudos.phantom.dsl._
import com.trifectalabs.osprey.v0.models._
import resources.CassandraConnector
import modules.DAOModule
import scala.concurrent.Future
import scala.language.implicitConversions

class StreamTable
  extends CassandraTable[StreamTable, Stream] {
  override lazy val tableName = "streams"
  implicit def streamType(str: String): StreamType = StreamType(str)

  object activity_id extends UUIDColumn(this) with PartitionKey[UUID]
  object stream_type extends StringColumn(this) with PrimaryKey[String]
  object data extends StringColumn(this)
  object series_type extends StringColumn(this)
  object original_size extends IntColumn(this)
  object resolution extends StringColumn(this)

  def fromRow(row: Row): Stream = {
    Stream(
      activity_id(row), stream_type(row), data(row), 
      series_type(row), original_size(row), resolution(row))
  }
}

object StreamTable extends StreamTable with CassandraConnector with DAOModule {
    def findByActivityID(activityID: UUID, streamType: Seq[StreamType] = StreamType.all): Future[Seq[Stream]] = {
      val list = streamType.map(_.toString).toList
      select.where(_.activity_id eqs activityID)
        .and(_.stream_type in list).fetch()
    }

    def add(stream: Stream): Future[ResultSet] = {
      insert
        .value(_.activity_id, stream.activityID)
        .value(_.stream_type, stream.streamType.toString)
        .value(_.data, stream.data)
        .value(_.series_type, stream.seriesType.toString)
        .value(_.original_size, stream.originalSize)
        .value(_.resolution, stream.resolution)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }

    def remove(stream: Stream): Future[ResultSet] = {
      delete
        .where(_.activity_id eqs stream.activityID)
        .and(_.stream_type eqs stream.streamType.toString)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
}
