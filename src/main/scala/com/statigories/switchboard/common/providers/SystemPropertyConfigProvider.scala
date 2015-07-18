package com.statigories.switchboard.common.providers

import scala.reflect.ClassTag

/**
 * Loads config from System properties, pushing them into the abstract type T.
 */
abstract class SystemPropertyConfigProvider[T : ClassTag] extends ConfigProvider[T] {
  override protected def propertyNameToValue(prefix: Option[String], name: String): Option[String] = {
    Option(System.getProperty(cleanupName(prefix, name)))
  }

  /**
   * System properties should be of the form: prefix.variableName
   */
  def cleanupName(prefix: Option[String], name: String): String = {
    prefix.map(p => s"${p.toLowerCase}.$name").getOrElse(name)
  }
}
