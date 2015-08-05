package com.example.locationservice

/**
 * @author Jörg
 */
import com.glueware.glue._
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

class LocationComponents()
    extends ServiceComponents
    with Directives
    with SprayJsonSupport {
  val googleLocate: FutureFunction1[Address, GoogleLocation] = GoogleLocate()
  val locate: FutureFunction1[Address, Location] = Locate(googleLocate)

  // wire the service, here it is simple just one function calling another
  // final: application logic is not allowed to be changed
  final def apply()(implicit executionContext: ExecutionContext): Set[ServiceComponent[_, _]] = {
    // in the service context the FutureFunction1 implicitly becomes a ServiceComponent
    Set(locate)
  }

//  override def route()(implicit executionContext: ExecutionContext): Route = {
//    logRequest("request") {
//      complete(OK, GoogleLocation(52.0618174, 5.1085974).toString)
//      //      path("""com/example/locationservice/Locate""") {
//      //        post {
//      //          entity(as[Address]) { input =>
//      //            produce(instanceOf[Location]) { output => complete(OK, GoogleLocation(52.0618174, 5.1085974).toString())
//      //              onComplete(Future(GoogleLocation(52.0618174, 5.1085974))) {
//      //                case Success(value)     => complete(OK, value.asInstanceOf[Location])
//      //                case Failure(exepction) => complete(InternalServerError, s"An error occurred: ${exepction.getMessage}")
//      //              }
//      //            }
//      //          }
//      //        }
//      //      }
//    }
//  }
}