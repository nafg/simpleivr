package simpleivr.testing

import org.scalatest.funsuite.AnyFunSuite


class UtilTests extends AnyFunSuite {
  test("Util.spans") {
    val actual = Util.spans(List(1, 2, 3, 4, 5))(_ == 3)
    assert(actual == List(List(1, 2), List(4, 5)))
  }
}
