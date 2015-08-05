package com.glueware.glue

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.OK
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.RootJsonFormat
import spray.routing.Directive.pimpApply
import spray.routing.Directives
import spray.routing.Route
import spray.routing.directives.OnCompleteFutureMagnet.apply

// Provides implicit conversions for various context
object ServiceComponent {
  implicit def serviceComponent[T, R](
    function: FutureFunction1[T, R])(
      implicit executionContext: ExecutionContext,
      jsonT: RootJsonFormat[T],
      jsonR: RootJsonFormat[R]) = new ServiceComponent[T, R](function)
}

// This class defines our service behavior for a function with the service actor as parameter
class ServiceComponent[T, R](function: FutureFunction1[T, R])(
  implicit val executionContext: ExecutionContext,
  jsonT: RootJsonFormat[T], // used by SprayJsonSupport
  jsonR: RootJsonFormat[R])
    extends Directives
    with SprayJsonSupport {

  val name: String = this.getClass.getName()

  // Produces the future required by onComplete
  def apply(input: T): Future[R] =
    function.apply(Future(input))

  // By convention we generate the path from the name
  val path = name.replaceAll(".", "/").replaceAll("$", "/")

  val route: Route =
    path(path) {
      post {
        entity(as[T]) { input =>
          produce(instanceOf[R]) { output =>
            onComplete(apply(input)) {
              case Success(value)     => complete(OK, value.asInstanceOf[R])
              case Failure(exepction) => complete(InternalServerError, s"An error occurred: ${exepction.getMessage}")
            }
          }
        }
      }
    }
}

