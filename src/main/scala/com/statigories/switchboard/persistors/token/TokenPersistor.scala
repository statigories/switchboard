package com.statigories.switchboard.persistors.token

import org.scribe.model.Token

object TokenType extends Enumeration {
  type TokenType = Value
  val Access, Request = Value
}

trait TokenPersistor {
  import TokenType._

  /**
   * Implement this to persist the given token across sessions.
   *
   * @param userUniqueIdentifier Something to uniquely identify the user that owns the token.
   * @param token The token to persist. This may need to be serialized based on your datastore.
   * @param tokenType The type of token that is being persisted for the user, should be stored separately from other types.
   */
  def persist(userUniqueIdentifier: String, token: Token, tokenType: TokenType): Unit

  /**
   * Implement this to pull the persisted token out of the datastore.
   *
   * @param uniqueIdentifier Something to uniquely identify the user that owns the token.
   * @param tokenType The type of token to lookup for the user.
   * @return
   */
  def lookup(uniqueIdentifier: String, tokenType: TokenType): Option[Token]
}
