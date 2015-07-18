package com.statigories.switchboard.apis

import com.statigories.switchboard.common.OAuthClient
import org.scribe.builder.api.FlickrApi

trait Flickr extends OAuthClient {
  override val apiType = classOf[FlickrApi]
  override val rootEndpoint = "https://api.flickr.com/services/rest/"

  def getActivity(userUniqueIdentifier: String): String = {
    getResponse(userUniqueIdentifier, buildGetRequest(parameters = Map(
      "method" -> "flickr.activity.userPhotos",
      "timeframe" -> "2d"
    )))
  }
}
