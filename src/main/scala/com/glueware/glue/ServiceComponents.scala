package com.glueware.glue

import scala.concurrent.ExecutionContext
import spray.routing.RouteConcatenation._
import spray.routing.Route

/**
 * @author Jörg
 */
abstract class ServiceComponents {
  def apply()(implicit executionContext: ExecutionContext): Set[ServiceComponent[_, _]]

  def route()(implicit executionContext: ExecutionContext): Route = {
    val routes = apply().map(_.route)

    // combine the routes
    // can be optimized by grouping parts of the paths
    routes.tail.fold[Route](routes.head)((s1, s2) => s1 ~ s2)
  }

}