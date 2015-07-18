package com.statigories.switchboard.exceptions

import java.io.IOException

case class HttpException(statusCode: Int, message: String) extends IOException(message)

object HttpException {
  def apply(statusCode: Int): HttpException = HttpException(statusCode, null)
}