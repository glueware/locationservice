package com.glueware.glue

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import spray.util.LoggingContext
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
 * By wrapping _apply( ... ) with the same signature as apply( ... ) the handling of various aspects may be done:
 * - Logging
 * - Observing by sending messages to other actors
 * - Stack trace reduced to FutureFunction
 * - etc.
 *
 * The _apply method has to be implemented by the developer
 */
trait FutureFunction /* extends Configuration - needs implicit ActorSystem*/ {
  self =>
  val name = self.getClass.getName
  //  val configEntry = name
}

/**
 * Message must not be used for construction an external error http response
 * Exception is included and may reveal information for attacks
 * Message is used for internal error logging
 */
case class InternalExceptionMessage(functionName: String, parameters: Seq[Future[_]], status: StatusCode, description: String, exception: Option[Exception])

/**
 * Message is used for construction an external error http response
 * Exception is excluded because of security concerns
 */
case class ExternalExceptionMessage(functionName: String, parameters: Seq[Future[_]], status: StatusCode, description: String)

/**
 * Unified interface for exceptions occurring functions
 */
trait FunctionException extends RuntimeException {
  val functionName: String
  val parameters: Seq[Future[_]]
  val status: StatusCode
  val description: String
  val exception: Option[Exception] = None
  val internalMessage = InternalExceptionMessage(functionName, parameters, status, description, exception).toString
  val externalMessage = ExternalExceptionMessage(functionName, parameters, status, description).toString
  override def getMessage() = externalMessage
}

trait FutureFunction0[+R] extends FutureFunction {
  self =>

  /**
   * Abstract method to be implemented
   */
  protected def _apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext, log: LoggingContext): Future[R]

  /**
   *
   */
  def apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext, log: LoggingContext): Future[R] = {
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

trait FutureFunction1[T, +R] extends FutureFunction {
  self =>

  /**
   * Abstract method to be implemented
   */
  protected def _apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext, log: LoggingContext): Future[R]
  def apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext, log: LoggingContext): Future[R] = {
    def beforeCall(parameter: Future[T]) {
      log.error(s"""calling ${name} with parameter ${parameter}""")
    }

    def afterCall(parameter: Future[T], returnValue: Future[R]) {
      returnValue.onComplete { r =>
        log.error(s"""returning from ${name} with value ${r}""")
      }
    }

    _apply(parameter)
  }
  case class FunctionException[T](parameter: Future[T], status: StatusCode, description: String, exception: Option[Throwable] = None) extends RuntimeException {
    val functionName = self.name
  }
}
