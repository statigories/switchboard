package com.statigories.switchboard.persistors.token

import com.statigories.switchboard.exceptions.NoTableForTokenTypeException

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item, Table}
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import org.scribe.model.Token

/**
 * Uses the default AWS authentication chain. See
 * http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html#credentials-default
 */
trait DynamoDbPersistor extends TokenPersistor {
  import TokenType._

  /**
   * Implementers must define this. These tables must already exist in DynamoDB. They should
   * also be unique by API/tokenType combination, otherwise you risk overwriting tokens for
   * a given user.
   */
  def tokenTableNames: Map[TokenType, String]

  // Some constants for storing in DynamoDB
  val PrimaryKeyName = "user_guid"
  val TokenAttr = "token_str" // 'token' is a reserved word in DynamoDB
  val TokenSecretAttr = "secret"

  val db = new DynamoDB(new AmazonDynamoDBClient())

  override def persist(userUniqueIdentifier: String, token: Token, tokenType: TokenType): Unit = {
    withTable(tokenType) { table =>
      table.putItem(
        new Item()
          .withPrimaryKey(PrimaryKeyName, userUniqueIdentifier)
          .withString(TokenAttr, token.getToken)
          .withString(TokenSecretAttr, token.getSecret)
      )
    }
  }

  override def lookup(userUniqueIdentifier: String, tokenType: TokenType): Option[Token] = {
    withTable(tokenType) { table =>
      val spec = new GetItemSpec()
        .withPrimaryKey(PrimaryKeyName, userUniqueIdentifier)
        .withProjectionExpression(s"$PrimaryKeyName, $TokenAttr, $TokenSecretAttr")

      Option(table.getItem(spec)) map { item =>
        new Token(item.getString(TokenAttr), item.getString(TokenSecretAttr))
      }
    }
  }

  protected def withTable[T](tokenType: TokenType)(f: Table => T): T = {
    tokenTableNames.get(tokenType) match {
      case None => throw NoTableForTokenTypeException(tokenType)
      case Some(tableName) => {
        val table = db.getTable(tableName)
        f(table)
      }
    }
  }
}
