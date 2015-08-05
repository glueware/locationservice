package com.glueware.glue

/**
 * @author Jörg
 */

import spray.routing.Route
import spray.routing.RouteConcatenation._
import spray.routing.HttpService
import akka.actor.ActorLogging
import akka.actor.Actor

class ServiceActor(route: Route)
    extends Actor
    with ActorLogging
    with HttpService {

  implicit def actorRefFactory = context

  def receive: Receive =
    runRoute(route)
}