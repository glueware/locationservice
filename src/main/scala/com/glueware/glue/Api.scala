package com.glueware.glue

/**
 * @author Jörg
 */

import scala.concurrent.ExecutionContext
import spray.routing.RouteConcatenation._
import spray.routing.Route
import spray.routing.Directives
import akka.event.LoggingAdapter
import spray.http.StatusCodes.OK
import com.typesafe.config.Config
import akka.actor.ActorRefFactory
import spray.httpx.SprayJsonSupport

/**
 * Combines the routes of ServiceComponents to a common route
 */
abstract class Api()(implicit functionContext: FunctionContext)
    extends Directives
    with SprayJsonSupport {

  import functionContext._

  /**
   * The method where the application logic is wired
   *
   */
  def apply(): Set[ApiComponent[_, _]]

  def route(): Route = {
    val routes = apply().map(_.route).toList

    // combine the routes
    // can be optimized by grouping parts of the paths
    routes match {
      case (head :: tail) => tail.fold[Route](head)((s1, s2) => s1 ~ s2)
      case Nil            => path("alive") { complete(OK) }
    }
  }
}

abstract class ApiFactory {
  def apply()(implicit functionContext: FunctionContext): Api
}

