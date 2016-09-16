package app

import play.api.i18n.{Lang, Messages}
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Play, Application, GlobalSettings}
import resources.SchemaGenerator

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    SchemaGenerator.generateAll
//    val runJobService = Play.current.configuration.getBoolean("job.service.bool").getOrElse(throw new RuntimeException("Could not read job service bool"))
 //   if (runJobService == true) {
      //new ActivityScheduler()
  //  }
  }
}
