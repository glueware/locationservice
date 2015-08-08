package com.example.locationservice

/**
 * @author Jörg
 */
import com.glueware.glue._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.OK
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.RootJsonFormat
import spray.routing.Directive.pimpApply
import spray.routing.Directives
import spray.routing.Route
import spray.util.LoggingContext

/**
 * The class where the application logic is wired
 * The components googleLocate and locate may be overriden for testing purposes
 * - mock function
 * - throwing exception
 * - producing a time out
 * 
 */
class LocationComponents()
    extends Service
    with Directives
    with SprayJsonSupport {
  val googleLocate: FutureFunction1[Address, GeocodingResult] = GeocodingLocate()
  val locate: FutureFunction1[Address, ServiceLocation] = Locate(googleLocate)

  // wire the service, here it is simple just one function calling another
  // final: application logic is not allowed to be changed
  final def apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext, log: LoggingContext): Set[ServiceComponent[_, _]] = {
    // in the service context the FutureFunction1 implicitly becomes a ServiceComponent
    Set(locate)
  }
}