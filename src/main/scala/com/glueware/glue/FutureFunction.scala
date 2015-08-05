package com.glueware.glue

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait FutureFunction[-T, +R]

trait FutureFunction0[+R] {
  def apply()(implicit executionContext: ExecutionContext): Future[R]
}

trait FutureFunction1[T, +R] {
  def apply(parameter: Future[T])(implicit executionContext: ExecutionContext): Future[R]
} 

