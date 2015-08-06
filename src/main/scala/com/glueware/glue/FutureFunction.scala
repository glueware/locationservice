package com.glueware.glue

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

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
 *
 * The _apply method has to be implemented by the developer
 */
trait FutureFunction

trait FutureFunction0[+R] extends FutureFunction {

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
}

trait FutureFunction1[T, +R] extends FutureFunction {
  /**
   * This is the method a developer has to implement
   */
  protected def _apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R]
  def apply(parameter: Future[T])(implicit refFactory: akka.actor.ActorRefFactory, executionContext: ExecutionContext): Future[R] = {
    // TODO: Aspects to be handled before call
    _apply(parameter)
    // TODO: Aspects to be handled after call
  }
} 

