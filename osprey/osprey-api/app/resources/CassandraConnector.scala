package resources

import java.net.InetAddress

import com.datastax.driver.core.{Session, Cluster}
import com.websudos.phantom.connectors._
import com.websudos.phantom.dsl.Session
import modules.ConfigModule

import scala.collection.JavaConversions._

trait CassandraConnector extends ConfigModule with SimpleConnector {
  val hosts = config.getString("cassandra.host").split(",")
  val keyspace: String = config.getString("cassandra.keyspace")

  val Connector = ContactPoints(hosts).keySpace(keyspace)

  implicit override def keySpace: KeySpace = KeySpace(keyspace)
  implicit lazy override val session: Session = Connector.session
}

