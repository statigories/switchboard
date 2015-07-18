package com.statigories.switchboard.common.providers.models

import com.statigories.switchboard.common.FromMap

case class OAuthCredentials(apiKey: String, apiSecret: String)

object OAuthCredentials extends FromMap[OAuthCredentials] {
  override implicit def fromMap(map: Map[String, String]): OAuthCredentials = OAuthCredentials(
    apiKey = get("apiKey", map),
    apiSecret = get("apiSecret", map)
  )
}
