package com.example.locationservice

/**
 * @author Jörg
 */
import com.glueware.glue._
import scala.concurrent.ExecutionContext

class LocationComponents()
    extends ServiceComponents {
  val googleLocate: FutureFunction1[Address, GoogleLocation] = GoogleLocate()
  val locationFunction: FutureFunction1[Address, Location] = Locate(googleLocate)

  // wire the service, here it is simple just one function calling another
  final def apply()(implicit executionContext: ExecutionContext): Set[ServiceComponent[_, _]] = {
    // in the service context the FutureFunction1 implicitly becomes a ServiceComponent
    Set(locationFunction)
  }
}