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
import akka.event.LoggingAdapter
import akka.pattern.AskTimeoutException
import com.typesafe.config.Config
import akka.actor.ActorRefFactory

/**
 * @author Jörg
 */

/**
 * Abstract interface of Client
 */
abstract class IClient[T, R](implicit functionContext: FunctionContext)
  extends FutureFunction1[T, R]
  with DefaultJsonProtocol

/**
 * Implements an abstract web access client.
 */
abstract class Client[T, R](implicit functionContext: FunctionContext)
    extends IClient[T, R] {

  import functionContext._

  /**
   * default value for timeout of client web request - may be overridden
   */
  implicit protected val timeout = Timeout(10 seconds)

  /**
   * Convert input parameter to HttpRequest
   */
  protected def inputToRequest(parameter: T): HttpRequest

  /**
   * Convert HttpResponse to return value
   */
  protected def responseToResult(response: HttpResponse, parameter: Option[Try[T]] /* for exception paramters*/ ): R

  /*
   * default pipeline may be overridden
   */
  protected val pipeline: SendReceive = sendReceive

  // Member declared in scala.Function1 
  final protected def _apply(parameter: Future[T]): Future[R] = {

    val response = for {
      p <- parameter
      r <- pipeline(inputToRequest(p))
    } yield r

    val result = Promise[R]()

    // Do error handling and
    // convert exceptions to to FunctionException
    response.onComplete {
      case Success(httpResponse) =>
        httpResponse.status match {
          case httpStatusCodes.OK => result.success(responseToResult(httpResponse, parameter.value))
          case s                  => result.failure(FunctionException1(parameter.value, httpStatusCodes.BadGateway, s"Google Geocoding responded with Http status code: ${s}"))
        }
      case Failure(exception: AskTimeoutException) =>
        result.failure(FunctionException1(parameter.value, httpStatusCodes.GatewayTimeout, s"Did not receive a timely response from Google Geocoding", Some(exception)))
      case Failure(exception) =>
        result.failure(FunctionException1(parameter.value, httpStatusCodes.InternalServerError, "Failed to request Google Geocoding due to an internal exception", Some(exception)))
    }
    result.future
  }
}

