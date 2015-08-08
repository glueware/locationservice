package com.example.locationservice

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.glueware.glue.FutureFunction1

import GeocodingStatusCodes.OK
import spray.http.StatusCodes.BadRequest
import spray.http.StatusCodes.InternalServerError
import spray.json.DefaultJsonProtocol

/**
 * Output class for ServiceLocation
 */
case class ServiceLocation(latitude: Double, lat: Double)

/**
 * Implicit conversions needed when wrapping GoogleLocate by a ServiceComponent
 * Unused in our example
 *
 * see package object
 */
trait LocateJsonProtocol extends DefaultJsonProtocol {
  implicit def serviceLocationJson = jsonFormat2(ServiceLocation)
}

/**
 * Delivers a location for an address.
 * If there is corresponding location for address it fails.
 * So in case of success one can rely that there is location.
 */
case class Locate(googleLocate: FutureFunction1[Address, GeocodingResult])
    extends FutureFunction1[Address, ServiceLocation] {

  // Members declared in scala.Function1 
  def _apply(address: Future[Address])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[ServiceLocation] = {
    def googleResultToLocation(geocodingResult: GeocodingResult): ServiceLocation = {

      import GeocodingStatusCodes._

      val status = geocodingResult.status
      val location = geocodingResult.location

      status match {
        case OK =>
          if (location.isDefined)
            ServiceLocation(location.get.lat, location.get.lng)
          else
            throw new FunctionException(address, InternalServerError, "Google Geocoding contract not correctly checked")
        case _ => throw new FunctionException(address, BadRequest, s"Google Geocoding no location found. Geocoding Status Code: ${status}")
      }
    }
    googleLocate(address).map(googleResultToLocation)
  }
}
