package controllers

import play.Logger
import play.api.mvc._
import play.api.libs.iteratee._
import models._

object Soap extends Controller {

  def index(environment: String, localTarget: String) = Action(parse.xml) {
    implicit request =>

      Logger.info("Request on environment:" + environment + " localTarget:" + localTarget)

      val service = Service.findByLocalTargetAndEnvironmentName(localTarget, environment)
      service.map {
        service =>
        // forward the request to the actual destination
          val client = new Client(service, request)
          client.sendRequestAndWaitForResponse

          // forward the response to the client
          SimpleResult(
            header = ResponseHeader(client.response.status, client.response.headers),
            body = Enumerator(client.response.body)) //
            .withHeaders("ProxyVia" -> "soapower").as(XML)

      }.getOrElse {
        val err = "environment " + environment + " with localTarget " + localTarget + " unknown"
        Logger.error(err)
        BadRequest(err)
      }

  }

  def printRequest(implicit r: play.api.mvc.RequestHeader) {
    Logger.info("method:" + r)
    Logger.info("headers:" + r.headers)
    //Logger.info("SoapAction:" + r.headers("SOAPACTION"))
    Logger.info("path:" + r.path)
    Logger.info("uri:" + r.uri)
    Logger.info("host:" + r.host)
    Logger.info("rawQueryString:" + r.rawQueryString)
  }

}
