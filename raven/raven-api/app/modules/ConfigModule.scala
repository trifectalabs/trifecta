package modules

import com.typesafe.config._

trait ConfigModule {
  lazy val config = ConfigFactory.load()  
}
