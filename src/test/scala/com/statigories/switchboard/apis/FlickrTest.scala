package com.statigories.switchboard.apis

import java.net.URL
import java.util.UUID

import com.statigories.switchboard.common.providers.DefaultConfigProvider
import com.statigories.switchboard.common.providers.models.{Token => TokenConfig}
import com.statigories.switchboard.persistors.token.{InMemoryPersistor, TokenType}
import org.scalatest.{FlatSpec, Matchers}
import org.scribe.model.Token

class FlickrTest extends FlatSpec with Matchers {
  // Supply config via either ENV variables or system properties.
  private object TokenConfigProvider extends DefaultConfigProvider[TokenConfig]
  val tokenConfig = TokenConfigProvider.getRequiredConfig(Some("Flickr"))
  val accessToken = new Token(tokenConfig.token, tokenConfig.tokenSecret)

  object Flickr extends Flickr with InMemoryPersistor {
    override val callbackUrl = new URL("http://www.localhost.com/callback")
  }

  "#getActivity" should "work" in {
    val userGuid = UUID.randomUUID().toString
    Flickr.persist(userGuid, accessToken, TokenType.Access)
    val activity = Flickr.getActivity(userGuid)
    activity should include("\"stat\":\"ok\"")
  }

  "#getPhotoCounts" should "work" in {
    val userGuid = UUID.randomUUID().toString
    Flickr.persist(userGuid, accessToken, TokenType.Access)
    val counts = Flickr.getPhotoCounts(userGuid)
    counts should include("\"stat\":\"ok\"")
  }
}
