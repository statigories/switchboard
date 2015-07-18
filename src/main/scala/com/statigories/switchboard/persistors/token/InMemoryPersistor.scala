package com.statigories.switchboard.persistors.token

import org.scribe.model.Token

/**
 * This is an implementation of TokenPersistor that can be used for tests;
 * would not recommend using this for a real implementation since it will
 * not persist across restarts.
 */
trait InMemoryPersistor extends TokenPersistor {
  import TokenType._
  var accessTokens: Map[String, Token] = Map.empty
  var requestTokens: Map[String, Token] = Map.empty

  override def persist(userUniqueIdentifier: String, token: Token, tokenType: TokenType): Unit = {
    tokenType match {
      case Access => accessTokens = accessTokens ++ Map(userUniqueIdentifier -> token)
      case Request => requestTokens = requestTokens ++ Map(userUniqueIdentifier -> token)
    }
  }

  override def lookup(userUniqueIdentifier: String, tokenType: TokenType): Option[Token] = {
    tokenType match {
      case Access => accessTokens.get(userUniqueIdentifier)
      case Request => requestTokens.get(userUniqueIdentifier)
    }
  }
}
