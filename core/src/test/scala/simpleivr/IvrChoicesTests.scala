package simpleivr

import org.scalatest.{FunSuite, Matchers}


class IvrChoicesTests extends FunSuite with Matchers {
  test("assignNums") {
    val ivrChoices = new IvrChoices(DummySayables)
    import ivrChoices._

    def defined(key: DTMF) = Choice(key, Sayable.Empty, ())

    def auto = Choice(Sayable.Empty, ())

    val actual =
      assignNums(List(defined(DTMF.`1`), auto, defined(DTMF.`2`), auto, auto, auto, auto, auto, auto, auto))
        .map(_.key)

    val expected =
      List(
        Some(DTMF.`1`),
        Some(DTMF.`3`),
        Some(DTMF.`2`),
        Some(DTMF.`4`),
        Some(DTMF.`5`),
        Some(DTMF.`6`),
        Some(DTMF.`7`),
        Some(DTMF.`8`),
        Some(DTMF.`9`),
        Some(DTMF.`0`)
      )

    actual shouldBe expected
  }
}
