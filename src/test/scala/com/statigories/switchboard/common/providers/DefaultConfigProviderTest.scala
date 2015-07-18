package com.statigories.switchboard.common.providers

import com.statigories.switchboard.common.FromMap

import org.scalatest.{FlatSpec, Matchers}

class DefaultConfigProviderTest extends FlatSpec with Matchers {

  case class TestClass(foo: String, bar: Int, baz: Option[BigDecimal]) {
    val dummyProperty = "should not break"
  }

  object TestClassProvider extends DefaultConfigProvider[TestClass]

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

  it should "return the populated TestClass when the config is provided via System properties" in {
    System.setProperty("foo", "abcd")
    System.setProperty("bar", "1234")
    System.setProperty("baz", "10.5")
    TestClassProvider.getConfig() shouldBe Some(TestClass(
      foo = "abcd",
      bar = 1234,
      baz = Some(BigDecimal(10.5))
    ))
  }

  ignore should "return the populated TestClass when the config is provided via ENV variables" in {
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
}