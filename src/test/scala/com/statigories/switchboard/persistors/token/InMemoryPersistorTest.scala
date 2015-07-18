package com.statigories.switchboard.persistors.token

import com.statigories.switchboard.TestHelper

import org.scalatest.{FunSpec, Matchers}

import java.util.UUID

class InMemoryPersistorTest extends FunSpec with Matchers with TestHelper {

  object TestPersistor extends InMemoryPersistor

  describe("InMemoryPersistor") {
    TokenType.values.foreach { tokenType =>
      val userGuid = UUID.randomUUID.toString
      val token = generateToken
      it(s"should work round-trip for $tokenType") {
        TestPersistor.persist(userGuid, token, tokenType)
        TestPersistor.lookup(userGuid, tokenType) shouldBe Some(token)
      }
    }

    it("should overwrite when the token already exists") {
      val userGuid = UUID.randomUUID.toString
      val token1 = generateToken
      val token2 = generateToken

      token1 shouldNot be(token2)

      TestPersistor.persist(userGuid, token1, TokenType.Request)
      TestPersistor.lookup(userGuid, TokenType.Request) shouldBe Some(token1)

      TestPersistor.persist(userGuid, token2, TokenType.Request)
      TestPersistor.lookup(userGuid, TokenType.Request) shouldBe Some(token2)
    }
  }
}
