package com.example

/**
 * @author Jörg
 */
import spray.json._
import spray.json.DefaultJsonProtocol

package object locationservice extends DefaultJsonProtocol {
  implicit def locationJson: RootJsonFormat[Location] = jsonFormat2(Location)
  implicit def addressJson: RootJsonFormat[Address] = jsonFormat1(Address)
}

