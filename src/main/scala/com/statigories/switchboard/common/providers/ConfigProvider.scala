package com.statigories.switchboard.common.providers

import com.statigories.switchboard.exceptions.ConfigPropertyNotFoundException

import scala.reflect._
import scala.util.{Failure, Success, Try}

/**
 * Implement this to define a method through which some sort of config can be provided
 * to the runtime environment.
 *
 * Define the abstract class T in order to determine the config class to return.
 */
abstract class ConfigProvider[T : ClassTag] {
  /**
   * Define this method to get the config value through the desired means. If the property is not
   * found through the implemented process, it should return a None so it can be dealt with
   * appropriately (object can't be populated if required, but it's okay if optional).
   *
   * @param name The name of the config property to retrieve.
   * @return The string value of the property, found through the implemented process. None if not found.
   */
  protected def propertyNameToValue(prefix: Option[String], name: String): Option[String]

  /**
   * Steps through all of the properties of the class and populates them. If it can't populate the
   * class completely, it will return a None, which can then be chained with other ConfigProviders
   * to search for the fully-defined config.
   *
   * @param f The class T must have an implicit method in its companion object, which translates
   *          from a Map[String, String] to the class itself.
   * @return Some[T] if the class can be fully populated via this provider; None if not.
   */
  def getConfig(prefix: Option[String] = None)(implicit f: Map[String, String] => T): Option[T] = {
    val propertyMap = classTag[T].runtimeClass.getDeclaredFields.map { field =>
      val valueOpt = propertyNameToValue(prefix, field.getName)
      field.getName -> valueOpt.orNull
    }.filterNot(_._2 == null).toMap
    Try {
      f(propertyMap)
    } match {
      case Success(obj) => Some(obj)
      case Failure(ex: ConfigPropertyNotFoundException) => None
      case Failure(ex) => throw ex
    }
  }
}
