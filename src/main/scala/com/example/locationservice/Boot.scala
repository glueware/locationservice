package com.example.locationservice

/**
 * @author Jörg
 */

import com.glueware.glue._

object Boot extends App {
  Server("locationsystem", new LocationComponents())
}