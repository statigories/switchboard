package com.statigories.switchboard.common

import com.statigories.switchboard.TestHelper
import com.statigories.switchboard.common.providers.models.OAuthCredentials
import com.statigories.switchboard.exceptions.{HttpException, MissingTokenException}
import com.statigories.switchboard.persistors.token.{TokenType, InMemoryPersistor}

import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scribe.builder.api.Api
import org.scribe.model.{Response, OAuthRequest, Verifier}
import org.scribe.oauth.OAuthService
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mock.MockitoSugar

import java.net.URL
import java.util.UUID


class OAuthClientTest extends FlatSpec with Matchers with MockitoSugar with TestHelper {
  val requestToken = generateToken
  val accessToken = generateToken

  val mockOAuthClient = mock[OAuthService]
  when(mockOAuthClient.getRequestToken).thenReturn(requestToken)
  when(mockOAuthClient.getAuthorizationUrl(meq(requestToken))).thenReturn(s"http://www.example.com/auth?oauth_token=${requestToken.getToken}")
  when(mockOAuthClient.getAccessToken(meq(requestToken), any[Verifier])).thenReturn(accessToken)
  doNothing.when(mockOAuthClient).signRequest(meq(requestToken), any[OAuthRequest])
  System.setProperty("testapi.apiKey", "test-api-key")
  System.setProperty("testapi.apiSecret", "test-api-secret")

  object TestApi extends OAuthClient with InMemoryPersistor {
    override val callbackUrl = new URL("http://www.example.com/callback")
    override val apiType = classOf[Api]
    override val rootEndpoint = "http://www.example.com/api"
    override lazy val client = mockOAuthClient
  }

  "#getRedirectUrl" should "work" in {
    val redirectUrl = TestApi.getRedirectUrl(UUID.randomUUID.toString)
    redirectUrl.getQuery should include(requestToken.getToken)
  }

  it should "persist the requestToken for future reference" in {
    val userGuid = UUID.randomUUID.toString
    TestApi.getRedirectUrl(userGuid)
    TestApi.lookup(userGuid, TokenType.Request) shouldBe Some(requestToken)
  }

  "#getAccessToken" should "work when the request token exists" in {
    val userGuid = UUID.randomUUID.toString
    TestApi.getRedirectUrl(userGuid)
    TestApi.getAccessToken(userGuid, "123456")
  }

  it should "persist the accessToken for future reference" in {
    val userGuid = UUID.randomUUID.toString
    TestApi.getRedirectUrl(userGuid)
    TestApi.getAccessToken(userGuid, "123456")
    TestApi.lookup(userGuid, TokenType.Access) shouldBe Some(accessToken)
  }

  it should "throw MissingTokenException exception when the request token does not exist" in {
    val userGuid = UUID.randomUUID.toString
    val exception = intercept[MissingTokenException] {
      TestApi.getAccessToken(userGuid, "123456")
    }
    exception.tokenType shouldBe TokenType.Request
    exception.message shouldBe s"Request token for user $userGuid was not found"
  }

  "#getResponse" should "work" in {
    val request = mock[OAuthRequest]
    val response = mock[Response]
    when(response.getCode).thenReturn(200)
    when(response.getBody).thenReturn("example response")
    when(request.send()).thenReturn(response)
    val userGuid = UUID.randomUUID.toString
    TestApi.getRedirectUrl(userGuid)
    TestApi.getAccessToken(userGuid, "123456")
    TestApi.getResponse(userGuid, request) shouldBe "example response"
  }

  it should "throw an exception if the response is not successful" in {
    val request = mock[OAuthRequest]
    val response = mock[Response]
    when(response.getCode).thenReturn(400)
    when(response.getMessage).thenReturn("example exception")
    when(request.send()).thenReturn(response)
    val userGuid = UUID.randomUUID.toString
    TestApi.getRedirectUrl(userGuid)
    TestApi.getAccessToken(userGuid, "123456")
    val exception = intercept[HttpException] {
      TestApi.getResponse(userGuid, request)
    }
    exception.statusCode shouldBe 400
    exception.message shouldBe "example exception"
  }

  "init" should "load OAuth config from the environment" in {
    TestApi.apiKey shouldBe "test-api-key"
    TestApi.apiSecret shouldBe "test-api-secret"
  }

  "#buildEndpoint" should "work without a path" in {
    TestApi.buildEndpoint(None) shouldBe "http://www.example.com/api"
  }

  it should "work with a path" in {
    TestApi.buildEndpoint(Some("foo")) shouldBe "http://www.example.com/api/foo"
  }

  it should "work with a path and eliminate extra slashes" in {
    TestApi.buildEndpoint(Some("/foo")) shouldBe "http://www.example.com/api/foo"

    object TestApi2 extends OAuthClient with InMemoryPersistor {
      override protected lazy val config = OAuthCredentials("test-api-key", "test-api-secret")
      override val callbackUrl = new URL("http://www.example.com/callback")
      override val apiType = classOf[Api]
      override val rootEndpoint = "http://www.example.com/api/"
    }
    TestApi2.buildEndpoint(Some("foo")) shouldBe "http://www.example.com/api/foo"
  }
}
