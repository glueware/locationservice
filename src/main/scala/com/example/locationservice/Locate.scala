package com.example.locationservice

/**
 * @author Jörg
 */
import scala.concurrent._
import com.glueware.glue._
import spray.json.DefaultJsonProtocol

case class ServiceLocation(latitude: Double, lat: Double)

/**
 * see package object
 */
trait LocateJsonProtocol extends DefaultJsonProtocol {
  implicit def serviceLocationJson = jsonFormat2(ServiceLocation)
  implicit def addressJson = jsonFormat1(Address)
}

case class Locate(googleLocate: FutureFunction1[Address, GoogleApiResult[Location]])
    extends FutureFunction1[Address, ServiceLocation] {

  // Members declared in scala.Function1 
  def _apply(address: Future[Address])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[ServiceLocation] = {
    def googleResultToLocation(googleResult: GoogleApiResult[Location]): ServiceLocation = {

      // TODO handle cases for results
      ServiceLocation(googleResult.results.head.lat, googleResult.results.head.lng)
    }
    googleLocate(address).map(googleResultToLocation)
  }
}
