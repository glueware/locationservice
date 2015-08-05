package com.glueware.glue

import akka.actor.ActorSystem

/**
 * @author Jörg
 */
trait Configuration {
  self =>

  protected val configEntry: String
  protected implicit val system: ActorSystem
  protected lazy val configuration = system.settings.config

  val settings: Settings

  class Settings {
    def getString(property: String): String =
      configuration.getString(configEntry + "." + property)

    def getInt(property: String): Int =
      configuration.getInt(configEntry + "." + property)

    def getBoolean(property: String): Boolean =
      configuration.getBoolean(configEntry + "." + property)

    def getObjectList(configEntry: String) =
      configuration.getObjectList(configEntry)

    def getObject(configEntry: String) =
      configuration.getObject(configEntry)
  }
}