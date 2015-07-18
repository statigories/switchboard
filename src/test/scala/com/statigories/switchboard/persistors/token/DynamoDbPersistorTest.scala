package com.statigories.switchboard.persistors.token

import com.statigories.switchboard.TestHelper
import com.statigories.switchboard.exceptions.NoTableForTokenTypeException
import com.statigories.switchboard.persistors.token.TokenType._

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import org.scalatest.{FunSpec, Matchers}

import java.util.UUID

class DynamoDbPersistorTest extends FunSpec with Matchers with TestHelper {

  object TestPersistor extends DynamoDbPersistor {
    override def tokenTableNames = Map(
      Access -> "test-access-tokens",
      Request -> "test-request-tokens"
    )
  }

  describe("DynamoDbPersistor") {
    TokenType.values.foreach { tokenType =>
      val userGuid = UUID.randomUUID.toString
      val token = generateToken
      it(s"should work round-trip for $tokenType") {
        TestPersistor.persist(userGuid, token, tokenType)
        TestPersistor.lookup(userGuid, tokenType) shouldBe Some(token)
      }
    }
  }

  it("should overwrite when the token already exists") {
    val userGuid = UUID.randomUUID.toString
    val token1 = generateToken
    val token2 = generateToken

    token1 shouldNot be(token2)

    TestPersistor.persist(userGuid, token1, Request)
    TestPersistor.lookup(userGuid, Request) shouldBe Some(token1)

    TestPersistor.persist(userGuid, token2, Request)
    TestPersistor.lookup(userGuid, Request) shouldBe Some(token2)
  }

  it("should throw an exception when the table mapping is missing") {
    object IncompleteTestPersistor extends DynamoDbPersistor {
      override val tokenTableNames = Map.empty[TokenType, String]
    }

    intercept[NoTableForTokenTypeException] {
      IncompleteTestPersistor.persist(UUID.randomUUID.toString, generateToken, Request)
    }

    intercept[NoTableForTokenTypeException] {
      IncompleteTestPersistor.lookup(UUID.randomUUID.toString, Request)
    }
  }

  it("should throw an exception when the DynamoDB table is missing") {
    object InvalidTestPersistor extends DynamoDbPersistor {
      override val tokenTableNames = Map(
        Request -> "invalid-table-name"
      )
    }

    intercept[ResourceNotFoundException] {
      InvalidTestPersistor.persist(UUID.randomUUID.toString, generateToken, Request)
    }

    intercept[ResourceNotFoundException] {
      InvalidTestPersistor.lookup(UUID.randomUUID.toString, Request)
    }
  }
}
