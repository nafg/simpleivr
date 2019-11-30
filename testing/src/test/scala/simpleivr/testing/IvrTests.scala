package simpleivr.testing

import scala.util.Try

import simpleivr._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


class IvrTests extends AnyFunSuite with Matchers with InteractionTest {
  val ivr = new Ivr(DummySayables)

  import ivr._


  test("sayAndGetDigit") {
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions) shouldBe None
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions.press("").press("1")) shouldBe Some(DTMF.`1`)
    sayAndGetDigit(Sayable.Empty).runWith()(Interactions.press("2")) shouldBe Some(DTMF.`2`)
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
