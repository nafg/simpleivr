package simpleivr

import org.scalatest.{FunSuite, Matchers}
import simpleivr.DummySayables._


class SayablesTests extends FunSuite with Matchers {
  numberWords(54) shouldBe Sayable.Many(List(fifty, four))
  numberWords(111) shouldBe Sayable.Many(List(one, hundred, and, eleven))
}
