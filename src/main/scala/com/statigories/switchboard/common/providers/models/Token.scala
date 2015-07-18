package com.statigories.switchboard.common.providers.models

import com.statigories.switchboard.common.FromMap

case class Token(token: String, tokenSecret: String)

object Token extends FromMap[Token] {
  override implicit def fromMap(map: Map[String, String]): Token = Token(
    token = get("token", map),
    tokenSecret = get("tokenSecret", map)
  )
}
