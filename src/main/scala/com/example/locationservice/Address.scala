package com.example.locationservice

import spray.json.DefaultJsonProtocol

/**
 * @author Jörg
 */

import spray.json._
import spray.json.DefaultJsonProtocol

/**
 * Wrapper around a address string
 * Used as input parameter in our example
 */
case class Address(address: String)

/**
 * Implicit conversions needed when wrapping a FutureFunction by a ApiComponent
 * 
 * see package object
 */
trait AddressJsonProtocol extends DefaultJsonProtocol {
  implicit def addressJson = jsonFormat1(Address)
}
