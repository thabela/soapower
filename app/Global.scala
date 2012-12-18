import java.util.{ GregorianCalendar, Date }
import models.{ UtilDate, Environment, RequestData }
import org.apache.http.protocol.RequestDate
import play.api._

import play.api.libs.concurrent._

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Starting Soapower...")

    // initialDelay: Duration : 10 minutes
    // frequency: Duration : 10 hours
    Akka.system.scheduler.schedule(10 minutes, 10 hours) {
      Environment.compileStats()
      Environment.purgeXmlData()
      Environment.purgeAllData()
    }
  }

  override def onStop(app: Application) {
    Logger.info("Soapower shutdown...")
  }

}
