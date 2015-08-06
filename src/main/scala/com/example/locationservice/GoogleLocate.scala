package com.example.locationservice

import scala.concurrent._
import com.glueware.glue._
import akka.util._
import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.can.Http
import spray.httpx.SprayJsonSupport._
import spray.client.pipelining._
import spray.util._
import spray.http.HttpRequest

case class Location(lat: Double, lng: Double)
case class GoogleApiResult[T](status: String, results: List[T])

/**
 * see package object
 */
trait GoogleJsonProtocol extends DefaultJsonProtocol {
  implicit val locationJson = jsonFormat2(Location)
  implicit def googleApiResultJson[T: JsonFormat] = jsonFormat2(GoogleApiResult.apply[T])
}
case class GoogleLocate()
    extends FutureFunction1[Address, GoogleApiResult[Location]]
    with DefaultJsonProtocol {

  // TODO: configure
  implicit val _ = Timeout(1000)

  // Members declared in scala.Function1 
  def _apply(value: Future[Address])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext) = {
    val googleLocateUri = """https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA"""

    // TODO: Not production ready. Use Argonaut here to analyse the HttpResponse.
    // Commit just for informational purpose.
    // GoogleLocate still not working. 
    // Do not use unmarshal, because detailed error handling not possible.

    //    val pipeline = sendReceive ~> unmarshal[GoogleApiResult[???]]
    //    pipeline(Get(googleLocateUri))
    Future(GoogleApiResult[Location]("OK", List(Location(5.0, 6.0))))
  }
}
