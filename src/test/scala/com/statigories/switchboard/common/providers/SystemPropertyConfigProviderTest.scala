package com.statigories.switchboard.common.providers

import com.statigories.switchboard.common.FromMap

import org.scalatest.{FlatSpec, Matchers}

class SystemPropertyConfigProviderTest extends FlatSpec with Matchers {

  case class TestClass(foo: String, bar: Int, baz: Option[BigDecimal]) {
    val dummyProperty = "should not break"
  }

  object TestClassProvider extends SystemPropertyConfigProvider[TestClass]

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

  it should "return the populated TestClass when the config is provided" in {
    System.setProperty("foo", "abcd")
    System.setProperty("bar", "1234")
    System.setProperty("baz", "10.5")
    TestClassProvider.getConfig() shouldBe Some(TestClass(
      foo = "abcd",
      bar = 1234,
      baz = Some(BigDecimal(10.5))
    ))
  }

  it should "return the populated TestClass even when optional properties are missing" in {
    System.setProperty("foo", "abcd")
    System.setProperty("bar", "1234")
    System.clearProperty("baz")
    TestClassProvider.getConfig() shouldBe Some(TestClass(
      foo = "abcd",
      bar = 1234,
      baz = None
    ))
  }

  "#cleanupName" should "work without a prefix" in {
    TestClassProvider.cleanupName(None, "apiKey") shouldBe "apiKey"
  }

  it should "include the prefix when given" in {
    TestClassProvider.cleanupName(Some("prefix"), "apiKey") shouldBe "prefix.apiKey"
  }
}
