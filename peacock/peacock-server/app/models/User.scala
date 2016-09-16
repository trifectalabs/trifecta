package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.trifectalabs.osprey.v0.models.UserActivityAttributes
import org.joda.time.DateTime
import utils.HardcodedData

/**
 * The user object.
 */
case class User(
  id: UUID,
  loginInfo: LoginInfo,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  email: Option[String] = None,
  city: Option[String] = None,
  province: Option[String] = None,
  country: Option[String] = None,
  sex: Option[String] = None,
  dateOfBirth: Option[DateTime] = None,
  age: Option[Int] = None,
  avatarURL: Option[String] = None,
  height: Option[Double] = None,
  weight: Option[Double] = None,
  attributes: Map[String, UserActivityAttributes] =
    HardcodedData.defaultAttributes()) extends Identity
