package simpleivr

import org.scalatest.{FunSuite, Matchers}
import simpleivr.DummySayables._


class SayablesTests extends FunSuite with Matchers {
  numberWords(54) shouldBe Sayable.Seq(List(fifty, four))
  numberWords(111) shouldBe Sayable.Seq(List(one, hundred, and, eleven))
}
