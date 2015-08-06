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

/*
 * Provides implicit conversions for various context
 */
object ServiceComponent {
  implicit def serviceComponent[T, R](
    function: FutureFunction1[T, R])(
      implicit refFactory: akka.actor.ActorRefFactory,
      executionContext: ExecutionContext,
      jsonT: RootJsonFormat[T],
      jsonR: RootJsonFormat[R]) = new ServiceComponent[T, R](function)
}

/*
 * This class defines our service behavior for a function 
 */
class ServiceComponent[T, R](function: FutureFunction1[T, R])(
  implicit val executionContext: ExecutionContext,
  implicit val refFactory: akka.actor.ActorRefFactory,
  jsonT: RootJsonFormat[T], // used by SprayJsonSupport
  jsonR: RootJsonFormat[R])
    extends Directives
    with SprayJsonSupport {

  /**
   * The name of a service component
   */
  val name: String = function.getClass.getName()

  /*
   * Produces the future required by onComplete
   */
  def apply(input: T): Future[R] =
    function.apply(Future(input))

  // By convention we generate the path from the name
  val items = name.split(Array('$', '.')).toList

  // We have to combine the items of name to a new "path" directive
  val _path = items match {
    case (head1 :: head2 :: tail) => path(((head1 / head2) /: tail)((x, y) => x / y))
    case (head1 :: Nil)           => path(head1)
    case Nil                      => noop
  }

  val route: Route =
    logRequest("request") {
      _path {
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
}

