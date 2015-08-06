package com.glueware.glue

/**
 * @author Jörg
 */

import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.routing.Route

case class Server(systemName: String, serviceFactory: Service) {
  server =>
  //  Construct the ActorSystem we will use in our application
  implicit private lazy val system: ActorSystem = ActorSystem(systemName)

  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook(system.shutdown())

  // get the execution environment
  implicit val _ = system.dispatcher

  private val route: Route = serviceFactory.route()

  private val listener = system.actorOf(Props(new ServiceActor(route)))

  // Get Server settings for Http.Bind
  private val configuration = new Configuration {
    val system = server.system
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
}
