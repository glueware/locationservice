package com.glueware.glue

/**
 * @author Jörg
 */

import spray.routing.Route
import spray.routing.RouteConcatenation._
import spray.routing.HttpService
import akka.actor.ActorLogging
import akka.actor.Actor
import spray.routing.HttpServiceActor
import akka.actor.Props
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

object ServiceActor {
  def apply(apiFactory: ApiFactory)() =
    new ServiceActor {
      implicit val functionContext = FunctionContext()(
        actorRefFactory = context,
        executionContext = system.dispatcher,
        configuration = system.settings.config,
        log = system.log)
      val route: Route = apiFactory.apply.route()
    }
}
abstract class ServiceActor
    extends Actor
    with HttpService {

  val system = context.system

  implicit val actorRefFactory = context
  val route: Route

  val receive: Receive =
    runRoute(route)
}