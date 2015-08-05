package com.glueware.glue

/**
 * @author Jörg
 */

import akka.actor.ActorSystem
import akka.actor.Props
import spray.routing.Route
import spray.routing.RouteConcatenation._
import akka.io.IO
import spray.can.Http

case class Server(systemName: String, serviceFactory: ServiceComponents) {
  server =>
  //  Construct the ActorSystem we will use in our application
  implicit lazy val system: ActorSystem = ActorSystem(systemName)

  // Ensure that the constructed ActorSystem is shut down when the JVM shuts down
  sys.addShutdownHook(system.shutdown())

  // get the execution environment
  implicit val _ = system.dispatcher

  // get the service components
  val serviceComponents: Set[ServiceComponent[_, _]] = serviceFactory()

  // get routes from service components
  val routes = serviceComponents.map(_.route)

  // combine the routes
  // can be optimized by grouping parts of the paths
  val route: Route = routes.tail.fold[Route](routes.head)((s1, s2) => s1 ~ s2)

  val listener = system.actorOf(Props(new ServiceActor(route)))

  // Get Server settings for Http.Bind
  val configuration = new Configuration {
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
