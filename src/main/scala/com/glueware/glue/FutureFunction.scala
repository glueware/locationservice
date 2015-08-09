package com.glueware.glue

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

import com.typesafe.config.Config

import akka.actor.ActorSystem
import spray.http.StatusCode

/**
 * FutureFunction my wrap various mechanisms to implement and return a Future.
 * The asynchronous behavior may hidden. For instance we might use form implementation:
 * - web services
 * - actors
 * - akka clusters
 * - etc.
 *
 * There is a difference to nowadays philosophy. Not the caller decides if the call is asynchronous but the callee. Is that OK?
 * If not then the function may be wrapped in a similar manner as for services - now with an actor.
 *
 * By wrapping _apply( ... ) with the same signature as apply( ... ) the handling of various aspects of cross cutting concerns may be done:
 * - Logging
 * - Observing by sending messages to other actors
 * - Stack trace reduced to FutureFunction
 * - etc.
 *
 * The _apply method has to be implemented by the developer
 */
abstract class FutureFunction(implicit system: ActorSystem) extends Configuration {
  self =>

  implicit val configuration: Config = system.settings.config
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val log = system.log

  val name = self.getClass.getName
  val configEntry = name
}

/**
 * Message must NOT be used for constructing an external error http response
 * Exception is included and may reveal information for attacks
 * Message is used for internal error logging
 */
case class InternalExceptionMessage(functionName: String, parameters: Seq[Option[Try[_]]], status: StatusCode, description: String, exception: Option[Exception])

/**
 * Message is used for constructing an external error http response
 * Exception is excluded because of security concerns
 */
case class ExternalExceptionMessage(functionName: String, parameters: Seq[Option[Try[_]]], status: StatusCode, description: String)

/**
 * Unified interface for exceptions occurring during function evaluation
 */
abstract class FunctionException extends RuntimeException {
  val functionName: String
  val parameters: Seq[Option[Try[_]]]
  val status: StatusCode
  val description: String
  val exception: Option[Exception] = None
  val internalMessage = InternalExceptionMessage(functionName, parameters, status, description, exception).toString
  val externalMessage = ExternalExceptionMessage(functionName, parameters, status, description).toString
  override def getMessage() = externalMessage
}

abstract class FutureFunction0[+R](implicit system: ActorSystem)
    extends FutureFunction {
  self =>

  /**
   * Abstract method to be implemented
   */
  protected def _apply(): Future[R]

  /**
   *
   */
  def apply(): Future[R] = {
    def beforeCall() {
      log.debug(s"""calling ${name}""")
    }

    def afterCall(returnValue: Future[R]) {
      log.debug(s"""returning from ${name} with paramter returnValue (${returnValue})""")
    }

    beforeCall()
    val returnValue = _apply()
    afterCall(returnValue)
    returnValue
  }

  case class FunctionException(status: StatusCode, description: String, exception: Option[Exception] = None) extends RuntimeException {
    val functionName = self.name
  }
}

abstract class FutureFunction1[T, +R](implicit system: ActorSystem)
    extends FutureFunction {
  self =>

  /**
   * Abstract method to be implemented
   */
  protected def _apply(parameter: Future[T]): Future[R]

  def apply(parameter: Future[T]): Future[R] = {
    def beforeCall(parameter: Future[T]) {
      parameter.onComplete { p =>
        log.debug(s"""calling ${name} with parameter ${p}""")
      }
    }

    def afterCall(parameter: Future[T], returnValue: Future[R]) {
      returnValue.onComplete { r =>
        log.debug(s"""returning from ${name} with value ${r}""")
      }
    }

    beforeCall(parameter)
    val returnValue = _apply(parameter)
    afterCall(parameter, returnValue)
    returnValue
  }
  case class FunctionException[T](parameter: Option[Try[T]], status: StatusCode, description: String, exception: Option[Throwable] = None) extends RuntimeException {
    val functionName = self.name
  }
}
