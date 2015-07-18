package com.statigories.switchboard.exceptions

import com.statigories.switchboard.persistors.token.TokenType.TokenType

case class NoTableForTokenTypeException(tokenType: TokenType) extends RuntimeException(s"No table found for token type $tokenType")