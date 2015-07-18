package com.statigories.switchboard.exceptions

import com.statigories.switchboard.persistors.token.TokenType.TokenType

case class MissingTokenException(tokenType: TokenType, message: String) extends RuntimeException(message)

object MissingTokenException {
  def apply(tokenType: TokenType): MissingTokenException = MissingTokenException(tokenType, null)
}