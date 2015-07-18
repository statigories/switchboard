package com.statigories.switchboard.common.providers

import scala.reflect.ClassTag

/**
 * Loads config from environment variables, pushing them into the abstract type T.
 */
abstract class EnvironmentVariableConfigProvider[T : ClassTag] extends ConfigProvider[T] {
  override protected def propertyNameToValue(prefix: Option[String], name: String): Option[String] = {
    Option(System.getenv(cleanupName(prefix, name)))
  }

  /**
   * ENV variables should be of the form: PREFIX_VARIABLE_NAME
   */
  def cleanupName(prefix: Option[String], name: String): String = {
    val underscoreName = name
      .replaceAll("([A-Z])", "_$1")
      .replaceAll("_+", "_")
      .replaceAll("^_", "")
      .replaceAll("_$", "")
    prefix.map(p => s"${p}_$underscoreName").getOrElse(underscoreName).toUpperCase
  }
}
