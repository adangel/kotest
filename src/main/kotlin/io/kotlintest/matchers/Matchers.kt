package io.kotlintest.matchers

import io.kotlintest.Inspectors

interface Keyword<K>

class MatcherBuilder<K, T>(val value: T)

interface MatchBuilder<K>

interface Matchers : StringMatchers,
    CollectionMatchers,
    DoubleMatchers,
    IntMatchers,
    LongMatchers,
    FileMatchers,
    MapMatchers,
    TypeMatchers,
    Inspectors {

  fun <T> equalityMatcher(expected: T) = object : Matcher<T> {
    override fun test(value: T): Result = Result(this == value, "$expected should equal $value")
  }

  fun fail(msg: String): Nothing = throw AssertionError(msg)

  infix fun Double.shouldBe(other: Double): Unit = should(ToleranceMatcher(other, 0.0))

  infix fun String.shouldBe(other: String) {
    if (this != other) {
      var msg = "String $this should be equal to $other"
      for (k in 0..Math.min(this.length, other.length) - 1) {
        if (this[k] != other[k]) {
          msg = "$msg (diverged at index $k)"
          break
        }
      }
      throw AssertionError(msg)
    }
  }

  infix fun <T> T.shouldBe(any: Any?): Unit = shouldEqual(any)
  infix fun <T> T.shouldEqual(any: Any?): Unit {
    when (any) {
      is Matcher<*> -> should(any as Matcher<T>)
      else -> {
        if (this == null && any != null)
          throw AssertionError(this.toString() + " did not equal $any")
        if (this != null && any == null)
          throw AssertionError(this.toString() + " did not equal $any")
        if (this != any)
          throw AssertionError(this.toString() + " did not equal $any")
      }
    }
  }

  infix fun <T> T.should(matcher: (T) -> Unit): Unit = matcher(this)

  @Deprecated("This syntax is deprecated, use `value should match`")
  infix fun <K, T> T.should(keyword: Keyword<K>) = MatcherBuilder<K, T>(this)

  infix fun <T> T.should(matcher: Matcher<T>): Unit {
    val result = matcher.test(this)
    if (!result.passed)
      throw AssertionError(result.message)
  }

  infix fun <T> T.shouldNotBe(any: Any?): Unit {
    when (any) {
      is Matcher<*> -> shouldNot(any as Matcher<T>)
      else -> shouldNot(equalityMatcher(this))
    }
  }

  infix fun <T> T.shouldNot(matcher: Matcher<T>): Unit {
    val result = matcher.test(this)
    if (result.passed)
      throw AssertionError("Test passed which should have failed: " + result.message)
  }
}