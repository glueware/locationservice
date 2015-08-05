package com.glueware.glue

import scala.concurrent.ExecutionContext

/**
 * @author Jörg
 */
abstract class ServiceComponents {
  def apply()(implicit executionContext: ExecutionContext): Set[ServiceComponent[_, _]]
}