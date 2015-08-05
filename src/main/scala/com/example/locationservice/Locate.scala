package com.example.locationservice

/**
 * @author Jörg
 */
import scala.concurrent._
import com.glueware.glue._

case class Location(latitude: Float, longitude: Float)

case class Locate(googleLocate: FutureFunction1[Address, GoogleLocation])
    extends FutureFunction1[Address, Location] {

  // Members declared in scala.Function1 
  def apply(address: Future[Address])(implicit executionContext: ExecutionContext): Future[Location] = {
    def googleResultToLocation(googleResult: GoogleLocation): Location = {
      Location(googleResult.latitude, googleResult.longitude)
    }
    googleLocate(address).map(googleResultToLocation)
  }
}
