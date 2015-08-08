package com.example.locationservice

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import akka.util.Timeout
import argonaut.Argonaut.StringToParseWrap
import argonaut.Argonaut.jArrayPL
import argonaut.Argonaut.jNumberPL
import argonaut.Argonaut.jObjectPL
import argonaut.Argonaut.jStringPL
import argonaut.Argonaut.jsonArrayPL
import argonaut.Argonaut.jsonObjectPL
import argonaut.Json
import spray.client.pipelining.Get
import spray.client.pipelining.sendReceive
import spray.http.HttpResponse
import spray.http.{ StatusCodes => httpStatusCodes }
import spray.json.DefaultJsonProtocol
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import com.glueware.glue.FutureFunction1
import scala.concurrent.Promise
import akka.pattern.AskTimeoutException

import scala.util.control.NonFatal

/**
 * The GeocodingStatusCodes express a contract with Google Geocoding.
 *
 * <a>https://developers.google.com/maps/documentation/geocoding/intro</a>
 *
 *
 *
 */
object GeocodingStatusCodes extends Enumeration {
  type GeocodingStatusCode = Value
  val OK, //indicates that no errors occurred; the address was successfully parsed and at least one geocode was returned. 
  ZERO_RESULTS, //indicates that the geocode was successful but returned no results. This may occur if the geocoder was passed a non-existent address
  OVER_QUERY_LIMIT, // indicates that you are over your quota.
  REQUEST_DENIED, // indicates that your request was denied.
  INVALID_REQUEST, //  generally indicates that the query (address, components or latlng) is missing.
  UNKNOWN_ERROR //  indicates that the request could not be processed due to a server error. The request may succeed if you try again.
  = Value
}

import GeocodingStatusCodes._

/**
 * Output classes for GeocodingLocate
 */
case class Location(lat: Double, lng: Double)
case class GeocodingResult(status: GeocodingStatusCodes.Value, location: Option[Location])

/**
 * Implicit conversions needed when wrapping GoogleLocate by a ServiceComponent
 * Unused in our example
 * see package object
 */
trait GoogleJsonProtocol extends DefaultJsonProtocol {
  trait GoogleStatusCodesJsonFormat extends JsonFormat[GeocodingStatusCode] {
    def write(code: GeocodingStatusCode) = JsString(code.toString)
    def read(value: JsValue) = value match {
      case JsString(string) => GeocodingStatusCodes.withName(string)
      case something        => spray.json.deserializationError("Expected GeocodingStatusCode as JsString, but got " + something)
    }
  }
  implicit object googleStatusCodesJsonFormat extends GoogleStatusCodesJsonFormat
  implicit val locationJson = jsonFormat2(Location)
  implicit val googleLocationJson = jsonFormat2(GeocodingResult)
}
/**
 * Uses Google Geocoding API.
 * The GeocodingStatusCodes express a contract with Google Geocoding.
 *
 * <a>https://developers.google.com/maps/documentation/geocoding/intro</a>
 *
 * Google: "Geocoding is the process of converting addresses (like "1600 Amphitheatre Parkway, Mountain View, CA")
 * into geographic coordinates (like latitude 37.423021 and longitude -122.083739),
 * which you can use to place markers on a map, or position the map."
 *
 */
case class GeocodingLocate()
    extends FutureFunction1[Address, GeocodingResult]
    with DefaultJsonProtocol {

  private val statusLens = jObjectPL >=>
    jsonObjectPL("status") >=>
    jStringPL

  private def status(parsedEntity: Option[Json]): Option[String] = {
    for {
      e <- parsedEntity
      s <- statusLens.get(e)
    } yield s
  }

  private val locationLens = jObjectPL >=>
    jsonObjectPL("results") >=>
    jArrayPL >=>
    jsonArrayPL(0) >=>
    jObjectPL >=>
    jsonObjectPL("geometry") >=>
    jObjectPL >=>
    jsonObjectPL("location") >=>
    jObjectPL

  private val latLens = locationLens >=>
    jsonObjectPL("lat") >=>
    jNumberPL

  private val lngLens = locationLens >=>
    jsonObjectPL("lng") >=>
    jNumberPL

  private def location(parsedEntity: Option[Json]): Option[Location] = {
    for {
      e <- parsedEntity
      lat <- latLens.get(e)
      lng <- lngLens.get(e)
    } yield Location(lat, lng)
  }

  /**
   * construct the end result
   */
  private def gecodingResult(httpResponse: HttpResponse, address: Future[Address]): GeocodingResult = {
    val parsedEntity = httpResponse.entity.asString.parseOption

    // extract by lenses from parsedEntity
    val _status = status(parsedEntity)
    val _location = location(parsedEntity)

    // check the contract with Google Geocoding 
    // see GeocodingStatusCodes
    _status match {
      case None =>
        throw FunctionException(address, httpStatusCodes.BadGateway, "Google Geocoding response did not return status")
      case Some("OK") if _location == None =>
        throw FunctionException(address, httpStatusCodes.BadGateway, "Google Geocoding response returned status OK but no location")
      case Some("ZERO_RESULTS") if _location != None =>
        throw FunctionException(address, httpStatusCodes.BadGateway, "Google Geocoding response returned status ZERO_RESULTS but also location(s)")
      case _ =>
        GeocodingResult(GeocodingStatusCodes.withName(_status.get), _location) // everything OK!
    }
  }

  // Member declared in scala.Function1 
  protected def _apply(address: Future[Address])(
    implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[GeocodingResult] = {
    import akka.util.Timeout
    import scala.concurrent.duration._
    implicit val timeout = Timeout(5 seconds)

    //    val googleLocateUri = URLEncode ...
    val uri = """https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA"""

    val pipeline = sendReceive
    val response = pipeline(Get(uri))
    response.map { r =>
      gecodingResult(r, address)
    }

    //    Future(GeocodingResult(OK, Some(Location(5, 3))))
    val result = Promise[GeocodingResult]()
    response.onComplete {
      case Success(value) => result.success(gecodingResult(value, address))
      case Failure(exception) => exception match {
        case exception: AskTimeoutException => FunctionException(address, httpStatusCodes.GatewayTimeout, s"Google Geocoding did not receive a timely response specified by URI (${uri})")
        case NonFatal(exception)            => FunctionException(address, httpStatusCodes.InternalServerError, "Google Geocoding failed to request URI (${uri}) due to an exception ()", Some(exception))
      }
    }
    result.future
  }
}
