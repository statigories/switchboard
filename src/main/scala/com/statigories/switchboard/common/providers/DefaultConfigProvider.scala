package com.statigories.switchboard.common.providers

import com.statigories.switchboard.exceptions.MissingConfigException

import scala.reflect._

/**
 * Loads config first from environment variables then from System properties, pushing them into the abstract type T.
 */
abstract class DefaultConfigProvider[T : ClassTag] extends ConfigProvider[T] {
  override protected def propertyNameToValue(prefix: Option[String], name: String): Option[String] = None // Not really needed

  private object EnvConfig extends EnvironmentVariableConfigProvider[T]
  private object SysPropConfig extends SystemPropertyConfigProvider[T]

  override def getConfig(prefix: Option[String] = None)(implicit f: Map[String, String] => T): Option[T] = {
    EnvConfig.getConfig(prefix)
      .orElse(SysPropConfig.getConfig(prefix))
  }

  /**
   * Load the config through the default chain; throw an exception if it is not found.
   */
  def getRequiredConfig(prefix: Option[String] = None)(implicit f: Map[String, String] => T): T = {
    getConfig(prefix).getOrElse(throw MissingConfigException(classTag[T].runtimeClass, prefix))
  }
}
