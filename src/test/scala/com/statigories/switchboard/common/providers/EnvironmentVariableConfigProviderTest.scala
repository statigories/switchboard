package com.statigories.switchboard.common.providers

import com.statigories.switchboard.TestHelper
import com.statigories.switchboard.common.FromMap

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class EnvironmentVariableConfigProviderTest extends FlatSpec with Matchers with MockitoSugar with TestHelper {

  case class TestClass(foo: String, bar: Int, baz: Option[BigDecimal]) {
    val dummyProperty = "should not break"
  }

  object TestClassProvider extends EnvironmentVariableConfigProvider[TestClass]

  object TestClass extends FromMap[TestClass] {
    override implicit def fromMap(map: Map[String, String]): TestClass = TestClass(
      foo = get("foo", map),
      bar = Integer.parseInt(get("bar", map)),
      baz = getOpt("baz", map).map(s => BigDecimal(s))
    )
  }

  "#getConfig" should "return None when the config is missing" in {
    TestClassProvider.getConfig() shouldBe None
  }

  ignore should "return the populated TestClass when the config is provided" in {
    // TODO: Figure out how to really test this.
//    System.setenv("foo", "abcd")
//    System.setenv("bar", "1234")
//    System.setenv("baz", "10.5")
    TestClassProvider.getConfig() shouldBe Some(TestClass(
      foo = "abcd",
      bar = 1234,
      baz = Some(BigDecimal(10.5))
    ))
  }

  "#cleanupName" should "delimit words with underscores" in {
    TestClassProvider.cleanupName(None, "apiKey") shouldBe "API_KEY"
  }

  it should "not include multiple underscores in a row" in {
    TestClassProvider.cleanupName(None, "api_Key") shouldBe "API_KEY"
  }

  it should "not include underscores at the beginning or end" in {
    TestClassProvider.cleanupName(None, "ApiKey_") shouldBe "API_KEY"
  }

  it should "include a prefix when given" in {
    TestClassProvider.cleanupName(Some("prefix"), "api_Key") shouldBe "PREFIX_API_KEY"
  }

}
