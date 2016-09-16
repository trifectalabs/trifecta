package modules

import com.typesafe.config.ConfigFactory

trait ConfigModule {
  lazy val config = ConfigFactory.load
}
