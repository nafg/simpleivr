package simpleivr

import org.scalatest.{FunSuite, Matchers}


class ChoiceMenuTests extends FunSuite with Matchers {
  test("assigned") {
    def defined(key: DTMF) = Choice(key, Sayable.Empty, ())

    def auto = Choice(Sayable.Empty, ())

    val choices = List(defined(DTMF.`1`), auto, defined(DTMF.`2`), auto, auto, auto, auto, auto, auto, auto)

    val choiceMenu = ChoiceMenu(Sayable.Empty, choices)

    val actual = choiceMenu.assigned.map(_.dtmf)

    val expected =
      List(DTMF.`1`, DTMF.`3`, DTMF.`2`, DTMF.`4`, DTMF.`5`, DTMF.`6`, DTMF.`7`, DTMF.`8`, DTMF.`9`, DTMF.`0`)

    actual shouldBe expected
  }
}
