package com.example.locationservice

import scala.concurrent._

import com.glueware.glue._

import spray.json._
import spray.json.DefaultJsonProtocol

case class GoogleLocation(latitude: Double, longitude: Double)

case class GoogleLocate()
    extends FutureFunction1[Address, GoogleLocation] {
  // Members declared in scala.Function1 
  def apply(value: Future[Address])(implicit executionContext: ExecutionContext): Future[GoogleLocation] =
    Future(GoogleLocation(52.0618174, 5.1085974))
}
