package com.glueware.glue

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.{ Success, Failure }
import scala.util.Try
import scala.concurrent.duration._
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.{ StatusCodes => httpStatusCodes }
import spray.client.pipelining.Get
import spray.client.pipelining.sendReceive
import spray.client.pipelining.SendReceive
import spray.util.LoggingContext
import akka.pattern.AskTimeoutException
import com.typesafe.config.Config
import akka.actor.ActorSystem

/**
 * @author Jörg
 */

/**
 *
 */
abstract class Client[T, R](implicit system: ActorSystem)
    extends FutureFunction1[T, R]
    with DefaultJsonProtocol {
  implicit val timeout = Timeout(10 seconds)
  protected def inputToRequest(parameter: T): HttpRequest
  protected def responseToResult(response: HttpResponse, parameter: Option[Try[T]] /* for exception paramters*/ ): R
  protected val pipeline: SendReceive = sendReceive

  // Member declared in scala.Function1 
  protected def _apply(parameter: Future[T]): Future[R] = {

    val response = for {
      p <- parameter
      r <- pipeline(inputToRequest(p))
    } yield r

    val result = Promise[R]()

    // convert exceptions to to FunctionException
    response.onComplete {
      case Success(value) =>
        result.success(responseToResult(value, parameter.value))
      case Failure(exception: AskTimeoutException) =>
        FunctionException(parameter.value, httpStatusCodes.GatewayTimeout, s"Did not receive a timely response from Google Geocoding", Some(exception))
      case Failure(exception) =>
        FunctionException(parameter.value, httpStatusCodes.InternalServerError, "Failed to request Google Geocoding due to an exception ()", Some(exception))
    }
    result.future
  }
}

