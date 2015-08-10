package com.example.locationservice

import org.specs2.mutable.Specification
import spray.http.ContentTypes._
import spray.http.HttpEntity
import spray.http.HttpMethods.POST
import spray.http.HttpRequest
import spray.http.StatusCodes.OK
import spray.http.Uri.apply
import spray.routing.HttpService
import spray.routing.directives.{ LoggingMagnet, LogEntry, DebuggingDirectives }
import spray.testkit.Specs2RouteTest
import akka.event.LoggingAdapter
import spray.routing.Directive.pimpApply
import spray.routing.directives.LoggingMagnet.forMessageFromMarker
import scala.concurrent.duration._
import akka.testkit._
import akka.actor.ActorSystem
import spray.httpx.SprayJsonSupport
import com.glueware.glue._

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocationServiceSpec
    extends Specification
    with Specs2RouteTest
    with HttpService
    with SprayJsonSupport {

  // get the execution environment
  implicit val excutionContext = system.dispatcher
  implicit val log = system.log
  def actorRefFactory = system

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(FiniteDuration(100, SECONDS))

  /*
     * Directives debugging
     */
  val logRequestResponsePrintln = {
    implicit val la: LoggingAdapter = null
    DebuggingDirectives.logRequest("location-service")

    def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
      case _ => Some(LogEntry(req.toString))
    }

    // This one doesn't use the implicit LoggingAdapter but uses `println` for logging
    def printRequestMethodAndResponseStatus(req: HttpRequest)(res: Any): Unit =
      println(requestMethodAndResponseStatusAsInfo(req)(res).map(_.obj.toString).getOrElse(""))
    DebuggingDirectives.logRequestResponse(LoggingMagnet(printRequestMethodAndResponseStatus))
  }

  implicit val functionContext = FunctionContext()(
    actorRefFactory = system,
    executionContext = system.dispatcher,
    configuration = system.settings.config,
    log = system.log)

  val route = LocationApi.apply().route()

  val json = """{"address": "Eendrachtlaan 315, Utrecht"}"""

  val locateUri = """/com/example/locationservice/Locate"""
  "location service" should {
    "return status OK POST " in {
      HttpRequest(method = POST, uri = locateUri,
        entity = HttpEntity(`application/json`, json)) ~> logRequestResponsePrintln(route) ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val serviceLocation = responseAs[ServiceLocation]
          serviceLocation.latitude must beCloseTo(52.0618174, 0.5)
          serviceLocation.longitude must beCloseTo(5.1085974, 0.5)
        }
    }
  }
}
