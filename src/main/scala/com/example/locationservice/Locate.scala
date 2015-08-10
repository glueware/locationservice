package com.example.locationservice

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.event.LoggingAdapter
import spray.http.StatusCodes.BadRequest
import spray.http.StatusCodes.InternalServerError
import spray.json.DefaultJsonProtocol

import com.glueware.glue._

import GeocodingStatusCodes.OK
import akka.actor.ActorRefFactory
import spray.http.StatusCodes.BadRequest
import spray.http.StatusCodes.InternalServerError
import spray.json.DefaultJsonProtocol
import akka.event.LoggingAdapter

/**
 * Output class for ServiceLocation
 */
case class ServiceLocation(latitude: Double, longitude: Double)

/**
 * Implicit conversions needed when wrapping GoogleLocate by a ApiComponent
 * Unused in our example
 *
 * see package object
 */
trait LocateJsonProtocol extends DefaultJsonProtocol {
  implicit def serviceLocationJson = jsonFormat2(ServiceLocation)
}

/**
 * Abstract interface of ILocate
 */
abstract class ILocate(geocodingLocate: FutureFunction1[Address, GeocodingResult])(implicit functionContext: FunctionContext)
  extends FutureFunction1[Address, ServiceLocation]

/**
 * Delivers a location for an address.
 * If there is corresponding location for address it fails.
 * So in case of success one can rely that there is location.
 */
case class Locate(geocodingLocate: FutureFunction1[Address, GeocodingResult])(implicit functionContext: FunctionContext)
    extends ILocate(geocodingLocate) {
  import functionContext._
  
  // Members declared in scala.Function1 
  def _apply(address: Future[Address]): Future[ServiceLocation] = {
    def geocodingResultToLocation(geocodingResult: GeocodingResult): ServiceLocation = {

      import GeocodingStatusCodes._

      val status = geocodingResult.status
      val location = geocodingResult.location

      status match {
        case OK =>
          if (location.isDefined)
            ServiceLocation(location.get.lat, location.get.lng)
          else
            throw FunctionException1(address.value, InternalServerError, "Google Geocoding contract not correctly checked")
        case _ => throw FunctionException1(address.value, BadRequest, s"Google Geocoding no location found. Geocoding Status Code: ${status}")
      }
    }
    geocodingLocate(address).map(geocodingResultToLocation)
  }
}
