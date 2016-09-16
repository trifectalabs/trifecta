package app

import play.api.{Play, Application, GlobalSettings}
import resources.SchemaGenerator

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    SchemaGenerator.generateAll
  }
}
