package com.statigories.switchboard.common

import com.statigories.switchboard.common.providers.DefaultConfigProvider
import com.statigories.switchboard.common.providers.models.OAuthCredentials
import com.statigories.switchboard.exceptions.{HttpException, MissingTokenException}
import com.statigories.switchboard.persistors.token.TokenType.TokenType
import com.statigories.switchboard.persistors.token.{TokenPersistor, TokenType}

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.Api
import org.scribe.model.{OAuthRequest, Token, Verb, Verifier}
import org.scribe.oauth.OAuthService

import java.net.{URLEncoder, URL}

trait OAuthClient extends TokenPersistor {
  /**
   * Implement these with the correct configuration for your environment.
   */
  def callbackUrl: URL
  def rootEndpoint: String

  protected object OAuthClientCredentialsConfigProvider extends DefaultConfigProvider[OAuthCredentials]
  /**
   * Override this if you don't want to automatically load config from your environment.
   */
  protected lazy val config: OAuthCredentials = OAuthClientCredentialsConfigProvider.getRequiredConfig(Some(this.getClass.getSimpleName.replaceAll("\\$$", "")))

  lazy val apiKey: String = config.apiKey
  lazy val apiSecret: String = config.apiSecret

  /**
   * Implement this to determine the API that is being used. See @org.scribe.builder.api._
   */
  def apiType: Class[_ <: Api]

  /**
   * A default OAuthService configuration based on the config above.
   */
  lazy val client: OAuthService = new ServiceBuilder()
    .provider(apiType)
    .apiKey(apiKey)
    .apiSecret(apiSecret)
    .callback(callbackUrl.toString)
    .build()

  /**
   * Builds the URL to redirect the user to, to authorize the OAuth connection.
   *
   * @param userUniqueIdentifier Something to uniquely identify the user that owns the token.
   * @return The URL to redirect to.
   */
  def getRedirectUrl(userUniqueIdentifier: String): URL = {
    val token = client.getRequestToken
    persist(userUniqueIdentifier, token, TokenType.Request)
    new URL(client.getAuthorizationUrl(token))
  }

  /**
   * Exchanges the verifier, using the previously-stored requestToken for the user, for the accessToken.
   *
   * If the exchange fails, an exception will be thrown; otherwise, the access token has been retrieved and persisted
   * successfully.
   *
   * @param userUniqueIdentifier Something to uniquely identify the user that owns the request and access tokens.
   * @param oauthVerifier The verifier returned by the service after a successful authorization.
   */
  def getAccessToken(userUniqueIdentifier: String, oauthVerifier: String): Unit = {
    withToken(userUniqueIdentifier, TokenType.Request) { token =>
      val accessToken = client.getAccessToken(token, new Verifier(oauthVerifier))
      persist(userUniqueIdentifier, accessToken, TokenType.Access)
    }
  }

  /**
   * Calls the API, based on the given request and the previously-acquired access token.
   *
   * This will throw an exception if it does not get an Http 200 result.
   *
   * @param userUniqueIdentifier
   * @param request The OAuthRequest, which is typically an HTTP method and URL endpoint for the API.
   * @return The results of the request.
   */
  def getResponse(userUniqueIdentifier: String, request: OAuthRequest): String = {
    withToken(userUniqueIdentifier, TokenType.Access) { token =>
      client.signRequest(token, request)
      val response = request.send()
      if (response.getCode >= 200 && response.getCode < 300) response.getBody
      else throw HttpException(response.getCode, response.getMessage)
    }
  }

  /**
   * Constructs the full endpoint by combining the root endpoint with the optional path.
   *
   * @param path Portion of the endpoint beyond the root, which is specific to this request.
   * @return The full endpoint.
   */
  def buildEndpoint(path: Option[String]): String = {
    path
      .map(_.replaceAll("^/+", ""))
      .map { p =>
        if (rootEndpoint.endsWith("/")) rootEndpoint + p
        else rootEndpoint + "/" + p
      }
      .getOrElse(rootEndpoint)
  }

  /**
   * Builds a GET URL request object to access the API.
   *
   * @param parameters The querystring parameters to include; will automatically include the apiKey and auth token.
   * @return
   */
  def buildGetRequest(path: Option[String] = None, parameters: Map[String, String] = Map.empty): OAuthRequest = {
    val url = buildEndpoint(path) + "?" + (parameters ++ Map("api_key" -> apiKey)).map { case (key, value) =>
        s"$key=${URLEncoder.encode(value, "UTF-8")}"
      }.mkString("&")
    new OAuthRequest(Verb.GET, url)
  }

  /**
   * Use this to wrap functionality that expects a token.
   */
  protected def withToken[T](userUniqueIdentifier: String, tokenType: TokenType)(f: Token => T): T = {
    lookup(userUniqueIdentifier, tokenType) match {
      case None => throw MissingTokenException(TokenType.Request, s"$tokenType token for user $userUniqueIdentifier was not found")
      case Some(token) => f(token)
    }
  }
}
