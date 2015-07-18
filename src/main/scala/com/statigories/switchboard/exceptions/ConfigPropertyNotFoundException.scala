package com.statigories.switchboard.exceptions

case class ConfigPropertyNotFoundException(propertyName: String) extends RuntimeException(s"Could not find property $propertyName")
