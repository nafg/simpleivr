package simpleivr.testing

import scala.util.Try

import org.scalatest.{FunSuite, Matchers}
import simpleivr._


class IvrTests extends FunSuite with Matchers with InteractionTest {
  val ivr = new Ivr(DummySayables)

  import ivr._


  test("sayAndGetDigit") {
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions) shouldBe None
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions.press("").press("1")) shouldBe Some('1')
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions.press("2")) shouldBe Some('2')
  }

  test("sayAndHandleDigits") {
    val step = sayAndHandleDigits(2, 4, Pause(0))(Function.unlift(str => Try(str.toInt).toOption))

    // Happy path
    step.runWith()(Interactions.press("1").press("2")) shouldBe Some(12)

    // Less than min digits
    step.runOnly()(Interactions.press("1").press("").waitForSayableWith("You must enter at least two digits"))

    // Second try
    step.runWith()(
      Interactions
        .press("1")
        .waitForSayableWith("You must enter at least two digits")
        .press("123")
    ) shouldBe Some(123)
  }
}
