package com.example.locationservice

import com.glueware.glue._

import akka.actor.ActorSystem
import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.OK
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.RootJsonFormat
import spray.routing.Directive.pimpApply
import spray.routing.Directives
import spray.util.LoggingContext

/**
 * The class where the application logic is wired
 * The components 'googleLocate' and 'locate' may be overriden for testing purposes
 * - mock function
 * - throwing exception
 * - producing a time out
 */
object ServiceApi
    extends ApiFactory {
  def create()(implicit system: ActorSystem): Api =
    new Api  {

      // wire the service, here it is simple, just one function calling another
      val googleLocate: FutureFunction1[Address, GeocodingResult] = GeocodingLocate()
      val locate: FutureFunction1[Address, ServiceLocation] = Locate(googleLocate)

      // final: application logic is not allowed to be changed
      final def apply(): Set[ApiComponent[_, _]] = {
        // in the service context the FutureFunction1 implicitly becomes a ApiComponent
        Set(locate)
      }
    }
}