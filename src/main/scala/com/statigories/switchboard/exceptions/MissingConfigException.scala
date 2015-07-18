package com.statigories.switchboard.exceptions

case class MissingConfigException(configClass: Class[_], prefix: Option[String]) extends RuntimeException(s"Missing ${configClass.getSimpleName} config with prefix [${prefix.getOrElse("")}]")
