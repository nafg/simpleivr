package simpleivr.testing

import cats.implicits._
import org.scalatest.{FunSuite, Matchers}
import simpleivr._


class InteractionTestTests extends FunSuite with Matchers with InteractionTest {
  test("Non-interactive computation") {
    val steps =
      IvrStep(10).flatMap { i =>
        IvrStep(15.5).flatMap { f =>
          IvrStep.amd.map(_ => true)
            .flatMap(b => IvrStep("" + b + i + f))
            .flatMap(str => IvrStep(s"'$str'"))
        }
      }
    steps.runWith()(Interactions) shouldBe "'true1015.5'"
  }

  test("Simple interactive computation") {
    val expects2 = IvrStep.waitForDigit(1) *> IvrStep.waitForDigit(2)
    expects2.runWith()(Interactions.press("1").press("2")) shouldBe Some('2')
  }
}
