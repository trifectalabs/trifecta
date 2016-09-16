package tables 

import com.websudos.phantom.dsl._
import com.trifectalabs.osprey.v0.models._
import java.util.UUID
import resources.CassandraConnector
import scala.concurrent.{Future, Await}
import scala.util._
import scala.concurrent.duration._
import org.joda.time.DateTime
import modules.DAOModule
import scala.language.implicitConversions

class UserTable extends CassandraTable[UserTable, User] {
  implicit def privacyType(str: String): PrivacyType = PrivacyType(str)
  implicit def roleType(str: String): RoleType = RoleType(str)
  implicit def calIDs(ids: Seq[String]): String = ids.mkString(",")
  implicit def calIDs(ids: String): Seq[String] = ids.split(",").toSeq
  override lazy val tableName = "users"

  object user_id extends UUIDColumn(this) with PartitionKey[UUID]
  object first_name extends StringColumn(this) 
  object last_name extends StringColumn(this) 
  object email extends StringColumn(this)
  object city extends StringColumn(this) 
  object province extends StringColumn(this) 
  object country extends StringColumn(this) 
  object sex extends OptionalStringColumn(this) 
  object date_of_birth extends OptionalDateTimeColumn(this) 
  object age extends OptionalIntColumn(this) 
  object follower_count extends IntColumn(this) 
  object avatar_url extends StringColumn(this) 
  object strava_token extends OptionalStringColumn(this) 
  object strava_avatar extends OptionalStringColumn(this) 
  object google_access_token extends OptionalStringColumn(this) 
  object google_refresh_token extends OptionalStringColumn(this) 
  object google_cal_ids extends OptionalStringColumn(this) 
  object privacy extends StringColumn(this) 
  object role extends StringColumn(this) 
  object created_at extends DateTimeColumn(this) 

  def fromRow(row: Row): User = {
    User(
      user_id(row), first_name(row), last_name(row), email(row),
      city(row), province(row), country(row), sex(row),
      date_of_birth(row), age(row), avatar_url(row),
      strava_token(row), strava_avatar(row), google_access_token(row),
      google_refresh_token(row), google_cal_ids(row).map(calIDs(_)), privacy(row), 
      role(row), created_at(row))
  }
}

class EmailToUserTable extends CassandraTable[EmailToUserTable, (String, UUID)] {
  override lazy val tableName = "email_to_users"

  object email extends StringColumn(this) with PartitionKey[String]
  object user_id extends UUIDColumn(this) with PrimaryKey[UUID]

  def fromRow(row: Row): (String, UUID) = (email(row), user_id(row))
}

object UserTable extends UserTable 
  with CassandraConnector with DAOModule {

    def getAll: Future[Seq[User]] = {
      select.fetch()
    }

    def getAllIDs: Future[Seq[UUID]] = {
      select(_.user_id).fetch()
    }

    def findByID(id: UUID): Future[Option[User]] = {
      select.where(_.user_id eqs id).one()
    }

    def findByEmail(email: String): Future[Option[User]] = {
      for {
        optID <- emailsToIDs.getUserIDFromEmail(email) 
        id = optID.getOrElse(new UUID(0,0))
        user <- select.where(_.user_id eqs id).one()
        } yield {
          user
        } 
    }

    def add(user: User): Future[ResultSet] = {
      EmailToUserTable.add(user.email, user.id) flatMap { a => 
        insert
          .value(_.user_id, user.id)
          .value(_.first_name, user.firstName)
          .value(_.last_name, user.lastName)
          .value(_.email, user.email)
          .value(_.city, user.city)
          .value(_.province, user.province)
          .value(_.country, user.country)
          .value(_.sex, user.sex)
          .value(_.date_of_birth, user.dateOfBirth)
          .value(_.age, user.age)
          .value(_.avatar_url, user.avatarURL)
          .value(_.strava_token, user.stravaToken)
          .value(_.strava_avatar, user.stravaAvatar)
          .value(_.google_access_token, user.googleCalendarAccessToken)
          .value(_.google_refresh_token, user.googleCalendarRefreshToken)
          .value(_.google_cal_ids, user.googleCalendarIDs.map(calIDs(_)))
          .value(_.privacy, user.privacy.toString)
          .value(_.role, user.role.toString)
          .value(_.created_at, user.createdAt)
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .future()
      }
    }

    def remove(id: UUID, email: String): Future[ResultSet] = {
      EmailToUserTable.remove(email) flatMap { a =>
        delete.where(_.user_id eqs id)
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .future()
      }
    }

    def amend(user: User): Future[ResultSet] = {
      update
        .where(_.user_id eqs user.id)
        .modify(_.google_access_token setTo user.googleCalendarAccessToken)
        .and(_.google_refresh_token setTo user.googleCalendarRefreshToken)
        .and(_.google_cal_ids setTo user.googleCalendarIDs.map(calIDs(_)))
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
  }

  object EmailToUserTable extends EmailToUserTable with CassandraConnector {
    def add(email: String, userID: UUID): Future[ResultSet] = {
      insert
        .value(_.email, email)
        .value(_.user_id, userID)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }

    def getUserIDFromEmail(email: String): Future[Option[UUID]] = {
      select.where(_.email eqs email).one().map(_.map(_._2))
    }
  
    def remove(email: String): Future[ResultSet] = {
      delete.where(_.email eqs email)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
  }
