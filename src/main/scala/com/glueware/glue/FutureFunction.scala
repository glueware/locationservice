package com.glueware.glue

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import spray.http.StatusCode

/**
 * FutureFunction my wrap various mechanisms to implement and return a Future.
 * The asynchronous behavior may hidden. For instance we might use form implementation:
 * - web services
 * - actors
 * - akka clusters
 * - etc.
 *
 * There is difference to nowadays implementations. Not the caller decides if the call is asynchronous but the callee.
 * Is that OK?
 * If not then the function may be wrapped in a similar manner as for services - now as actor.
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

trait FunctionException extends RuntimeException {
  val functionName: String
  val parameters: Seq[Future[_]]
  val status: StatusCode
  val description: String
  val exception: Option[Exception] = None
  override def getMessage() = s"""functionName: ${functionName}, parameters: ${parameters}, status: ${status}, description: ${description})"""
}

trait FutureFunction0[+R] extends FutureFunction {
  self =>

  /**
   * This is the method a developer has to implement
   */
  protected def _apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R]
  def apply()(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R] = {
    def beforeCall() {
      // TODO: Aspects to be handled before call      
    }
    def afterCall() {
      // TODO: Aspects to be handled after call      
    }

    beforeCall()
    val returnValue = _apply()
    afterCall()
    returnValue
  }

  case class FunctionException(status: StatusCode, description: String, exception: Option[Exception] = None) extends RuntimeException {
    val functionName = self.name
  }
}

trait FutureFunction1[T, +R] extends FutureFunction {
  self =>
  /**
   * This is the method a developer has to implement
   */
  protected def _apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R]
  def apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R] = {
    // TODO: Aspects to be handled before call
    _apply(parameter)
    // TODO: Aspects to be handled after call
  }
  case class FunctionException[T](parameter: Future[T], status: StatusCode, description: String, exception: Option[Throwable] = None) extends RuntimeException {
    val functionName = self.name
  }
}


