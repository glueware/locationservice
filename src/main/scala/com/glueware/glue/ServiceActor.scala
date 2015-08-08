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
  def apply(serviceFactory: ApiFactory)(implicit system: ActorSystem) = {
    val serviceActor = new ServiceActor {
      val route: Route = serviceFactory.create.route()
    }
    system.actorOf(Props(serviceActor))
  }
}
abstract class ServiceActor
    extends Actor
    with HttpService
    with ActorLogging {

  val actorRefFactory = context
  val route: Route

  val receive: Receive =
    runRoute(route)
}