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
  def props(apiFactory: ApiFactory): Props = Props(new ServiceActor(apiFactory))
}

/**
 * ServiceActor quite on the sense of spray
 */
class ServiceActor(apiFactory: ApiFactory)
    extends Actor
    with HttpService {

  val system = context.system

  implicit val functionContext = FunctionContext()(
    actorRefFactory = context,
    executionContext = system.dispatcher,
    configuration = system.settings.config,
    log = system.log)

  implicit val actorRefFactory = context
  val route: Route = apiFactory.apply.route()
  
  val receive: Receive =
    runRoute(route)
}