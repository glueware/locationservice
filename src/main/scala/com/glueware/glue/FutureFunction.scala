package com.glueware.glue

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

import com.typesafe.config.Config

import akka.actor.Actor
import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import spray.http.StatusCode

case class FunctionContext(implicit actorRefFactory: ActorRefFactory, executionContext: ExecutionContext, configuration: Config, log: LoggingAdapter)
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
abstract class FutureFunction(implicit functionContext: FunctionContext)
    extends Configuration {
  self =>

  val name = self.getClass.getName
  val configEntry = name
  lazy val configuration = ??? // functionContext.configuration
}

/**
 * Unified interface for exceptions occurring during function evaluation
 */
abstract class FunctionException extends RuntimeException {
  val functionName: String
  val parameters: Seq[Option[Try[_]]]
  val status: StatusCode
  val description: String
  val exception: Option[Throwable]
  val internalMessage = InternalExceptionMessage(functionName, parameters, status, description, exception)
  val externalMessage = ExternalExceptionMessage(functionName, parameters, status, description)
  override def getMessage() = externalMessage.toString()

  /**
   * Message must NOT be used for constructing an external error http response
   * Exception is included and may reveal information for attacks
   * Message is used for internal error logging
   */
  case class InternalExceptionMessage(functionName: String, parameters: Seq[Option[Try[_]]], status: StatusCode, description: String, exception: Option[Throwable])

  /**
   * Message is used for constructing an external error http response
   * Exception is excluded because of security concerns
   */
  case class ExternalExceptionMessage(functionName: String, parameters: Seq[Option[Try[_]]], status: StatusCode, description: String)
}

/**
 * Abstract FutureFunction with no parameter
 */
abstract class FutureFunction0[+R](implicit functionContext: FunctionContext)
    extends FutureFunction {
  self =>

  import functionContext._

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

  case class FunctionException0(status: StatusCode, description: String, exception: Option[Throwable] = None) extends FunctionException {
    val functionName = self.name
    val parameters: Seq[Option[Try[_]]] = Seq()
  }
}

/**
 * Abstract FutureFunction with one parameter
 */
abstract class FutureFunction1[T, +R](implicit functionContext: FunctionContext)
    extends FutureFunction {
  self =>
  import functionContext._

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
  case class FunctionException1[T](parameter: Option[Try[T]], status: StatusCode, description: String, exception: Option[Throwable] = None) extends FunctionException {
    val functionName = self.name
    val parameters: Seq[Option[Try[_]]] = Seq(parameter)
  }
}
