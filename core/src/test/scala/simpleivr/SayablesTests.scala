package simpleivr

import simpleivr.DummySayables._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


class SayablesTests extends AnyFunSuite with Matchers {
  numberWords(54) shouldBe Sayable.Many(List(fifty, four))
  numberWords(111) shouldBe Sayable.Many(List(one, hundred, and, eleven))
}
