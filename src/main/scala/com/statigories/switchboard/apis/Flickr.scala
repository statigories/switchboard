package com.statigories.switchboard.apis

import java.util.Date

import com.statigories.switchboard.common.OAuthClient
import org.scribe.builder.api.FlickrApi

trait Flickr extends OAuthClient {
  override val apiType = classOf[FlickrApi]
  override val rootEndpoint = "https://api.flickr.com/services/rest/"
  protected val BaseParameters = Map(
    "format" -> "json"
  )

  def getActivity(userUniqueIdentifier: String): String = {
    getResponse(userUniqueIdentifier, buildGetRequest(parameters = BaseParameters ++ Map(
      "method" -> "flickr.activity.userPhotos",
      "timeframe" -> "4h"
    )))
  }

  // TODO: Clean up the dates; likely needs a time zone for the user.
  def getPhotoCounts(userUniqueIdentifier: String): String = {
    val now = (new Date).getTime
    val oneDayMs: Long = 1000 * 60 * 60 * 24
    getResponse(userUniqueIdentifier, buildGetRequest(parameters = BaseParameters ++ Map(
      "method" -> "flickr.photos.getCounts",
      "dates" -> s"${now - (3L * oneDayMs)},${now - (2L * oneDayMs)},${now - (1L * oneDayMs)},$now"
    )))
  }
}
