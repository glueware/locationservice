/**
 * 
 */
package com.example

/**
 * @author Jörg
 */
import spray.json._
import spray.json.DefaultJsonProtocol

package object locationservice
    extends AddressJsonProtocol
    with LocateJsonProtocol
    with GoogleJsonProtocol {
}

