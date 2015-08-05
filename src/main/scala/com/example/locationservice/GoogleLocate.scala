package com.example.locationservice

import scala.concurrent._

import com.glueware.glue._

import spray.json._
import spray.json.DefaultJsonProtocol

case class GoogleLocation(latitude: Float, longitude: Float)

case class GoogleLocate()
    extends FutureFunction1[Address, GoogleLocation] {
  // Members declared in scala.Function1 
  def apply(value: Future[Address])(implicit executionContext: ExecutionContext): Future[GoogleLocation] = ???
}
