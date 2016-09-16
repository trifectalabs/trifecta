package modules

import com.trifectalabs.osprey.v0.Client
import com.trifectalabs.osprey.v0._

trait OspreyModule extends ConfigModule {
  lazy val ospreyUri = config.getString("services.osprey.uri")
  lazy val ospreyPort = config.getString("services.osprey.port")

  lazy val userActivityAttributesClient: UserActivityAttributes = new Client(s"$ospreyUri:$ospreyPort").userActivityAttributes
  lazy val userClient: Users = new Client(s"$ospreyUri:$ospreyPort").users
}
