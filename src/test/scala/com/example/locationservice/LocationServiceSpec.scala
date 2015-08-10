package com.example.locationservice

import org.specs2.mutable.Specification
import spray.http.ContentTypes._
import spray.http.HttpEntity
import spray.http.HttpMethods.POST
import spray.http.HttpRequest
import spray.http.StatusCodes._
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

  // producing the same route as in 
  implicit val functionContext = FunctionContext()(
    actorRefFactory = system,
    executionContext = system.dispatcher,
    configuration = system.settings.config,
    log = system.log)

  val route = LocationApi.apply().route()

  val locateUri = """/com/example/locationservice/Locate"""
  "location service" should {
    object Utrecht {
      val json = """{"address": "Eendrachtlaan 315, Utrecht"}"""
      val latitude = 52.0618174
      val longitude = 5.1085974
    }
    "return status OK and the correct result" in {
      HttpRequest(method = POST, uri = locateUri,
        entity = HttpEntity(`application/json`, Utrecht.json)) ~> route ~> check {
          response.status should be equalTo OK
          response.entity should not be equalTo(None)
          val serviceLocation = responseAs[ServiceLocation]
          serviceLocation.latitude must beCloseTo(52.0618174, 0.5)
          serviceLocation.longitude must beCloseTo(5.1085974, 0.5)
        }
    }
    object NonExistentAddress {
      val json = """{"address": "Xyz 123, Abc"}"""
    }
    "return status BadRequest for a non-existent address" in {
      HttpRequest(method = POST, uri = locateUri,
        entity = HttpEntity(`application/json`, NonExistentAddress.json)) ~> route ~> check {
          response.status should be equalTo BadRequest
        }
    }
  }

  // The extracted abstract Interfaces. e.g. ILocationApi, ILocate, IGeocodingLocate, ...
  // enable us to construct various test cases for FutureFunctions if the function(s) depending on is(are) malfunctioning     
  // ...
  // TODO
}
