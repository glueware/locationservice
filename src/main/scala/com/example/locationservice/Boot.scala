package com.example.locationservice

/**
 * @author Jörg
 */
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.routing.Route
import com.glueware.glue._

object Boot extends App {
  val server = Server("locationsystem", LocationApi)
}