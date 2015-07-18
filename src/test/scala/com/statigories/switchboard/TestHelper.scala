package com.statigories.switchboard

import org.scribe.model.Token

import scala.util.Random

trait TestHelper {
  def generateToken = new Token(Random.nextString(20), Random.nextString(10))
}