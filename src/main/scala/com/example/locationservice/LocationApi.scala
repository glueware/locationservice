package com.example.locationservice

import com.glueware.glue._

import akka.actor.ActorRefFactory
import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.OK
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.RootJsonFormat
import spray.routing.Directive.pimpApply
import spray.routing.Directives
import akka.event.LoggingAdapter

/**
 * The class where the application logic is wired
 * The components 'geocodingLocate' and 'locate' may be overriden for testing purposes
 * - mock function
 * - throwing exception
 * - producing a time out
 */
object LocationApi
    extends ApiFactory {

  def apply()(implicit functionContext: FunctionContext): Api =
    new ILocationApi {
      import functionContext._

      // wire the service, here it is simple, just one function calling another
      val geocodingLocate: FutureFunction1[Address, GeocodingResult] = GeocodingLocate()
      val locate: FutureFunction1[Address, ServiceLocation] = Locate(geocodingLocate)
    }
}

/**
 * The abstract ILocationApi
 */
abstract class ILocationApi(implicit functionContext: FunctionContext)
    extends Api {
  import functionContext._

  val geocodingLocate: FutureFunction1[Address, GeocodingResult]
  val locate: FutureFunction1[Address, ServiceLocation]

  // wire the service, here it is simple, just one function calling another
  def apply(): Set[ApiComponent[_, _]] = {
    // in the service context the FutureFunction1 implicitly becomes a ApiComponent
    Set(locate)
  }
}