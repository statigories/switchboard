package com.statigories.switchboard.common

import com.statigories.switchboard.exceptions.ConfigPropertyNotFoundException

trait FromMap[T] {
  /**
   * Implement this as an implicit on your companion object, to be used by @ConfigProvider
   *
   * Use the provided get and getOpt methods so exceptions roll up correctly.
   *
   * @param map The list of properties and values to load into the class.
   * @return The populated class.
   */
  def fromMap(map: Map[String, String]): T

  protected def get(propertyName: String, map: Map[String, String]): String = {
    val valueOpt = getOpt(propertyName, map)
    if (valueOpt.isEmpty) throw ConfigPropertyNotFoundException(propertyName)
    else valueOpt.get
  }

  protected def getOpt(propertyName: String, map: Map[String, String]): Option[String] = {
    map.get(propertyName)
  }
}
