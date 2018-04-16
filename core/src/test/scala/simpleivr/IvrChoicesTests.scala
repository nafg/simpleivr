package simpleivr

import org.scalatest.{FunSuite, Matchers}


class IvrChoicesTests extends FunSuite with Matchers {
  test("assignNums") {
    val ivrChoices = new IvrChoices(DummySayables)
    import ivrChoices._

    def defined(char: Char) = Choice(char, Sayable.Empty, ())

    def auto = Choice(Sayable.Empty, ())

    val actual =
      assignNums(List(defined('1'), auto, defined('2'), auto, auto, auto, auto, auto, auto, auto))
        .map(_.key)

    val expected =
      List(Some('1'), Some('3'), Some('2'), Some('4'), Some('5'), Some('6'), Some('7'), Some('8'), Some('9'), Some('0'))

    actual shouldBe expected
  }
}
