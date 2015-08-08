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
import spray.util.LoggingContext

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
 * and provides a route
 */
class ServiceComponent[T, R](function: FutureFunction1[T, R])(
  implicit val refFactory: akka.actor.ActorRefFactory,
  val executionContext: ExecutionContext,
  log: LoggingContext,
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

  // constructing the route

  /**
   * By convention we generate the path from the name
   * We have to combine the $ or . separated components of the  name to a new "path" directive _path
   * This could be improved by dropping the first components e.g. "com.glueware.glue"
   */
  private val _path = {
    val items = name.split(Array('$', '.')).toList

    items match {
      case (head1 :: head2 :: tail) => path(((head1 / head2) /: tail)((x, y) => x / y))
      case (head1 :: Nil)           => path(head1)
      case Nil                      => noop
    }
  }

  /**
   * partial route which is composed by service to the route of the server
   */
  val route: Route =
    _path {
      post {
        entity(as[T]) { input =>
          produce(instanceOf[R]) { output =>
            onComplete(apply(input)) {
              case Success(value) => complete(OK, value)
              case Failure(exception: FunctionException) => {
                log.error(exception.getMessage())
                complete(exception.status, s"An error occurred in service ${name} at function ${exception.functionName}: ${exception.description}")
              }
              case Failure(exception) => {
                log.error(exception.getMessage())
                complete(InternalServerError, s"An error occurred in service ${name}: ${exception.getMessage}")
              }
            }
          }
        }
      }
    }
}

