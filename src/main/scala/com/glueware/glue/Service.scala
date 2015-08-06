package com.glueware.glue

import scala.concurrent.ExecutionContext
import spray.routing.RouteConcatenation._
import spray.routing.Route
import spray.routing.Directives

/**
 * @author Jörg
 */
abstract class Service extends Directives {
  def apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Set[ServiceComponent[_, _]]

  def route()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Route = {
    val routes = apply().map(_.route).toList

    // combine the routes
    // can be optimized by grouping parts of the paths
    routes match {
      case (head :: tail) => tail.fold[Route](head)((s1, s2) => s1 ~ s2)
      case Nil            => (_ => ())
    }
  }
}