package com.glueware.glue

/**
 * @author Jörg
 */

import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.routing.Route

case class Server(systemName: String, serviceFactory: ApiFactory) {
  server =>
  //  Construct the ActorSystem we will use in our application
  implicit private lazy val system: ActorSystem = ActorSystem(systemName)

  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook(system.shutdown())

  // get the execution environment
  //  implicit val executionContext = system.dispatcher

  private val serviceActor = ServiceActor(serviceFactory)
  private val listener = system.actorOf(Props(serviceActor))

  // Get Server settings for Http.Bind
  private val configuration = new Configuration {
    val configuration = system.settings.config
    val configEntry = systemName
    lazy val settings = new Settings {
      val interface: String = getString("interface")
      val port: Int = getInt("port")
    }
  }

  import configuration.settings._
  // Start Http server
  IO(Http)(system) ! Http.Bind(
    listener = listener,
    interface = interface,
    port = port)

  // http://stackoverflow.com/questions/24731242/spray-can-webservice-graceful-shutdown
  // http://flurdy.com/docs/scalainit/startscala.html
}
