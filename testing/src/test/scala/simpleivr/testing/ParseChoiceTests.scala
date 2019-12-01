package simpleivr.testing

import simpleivr._
import org.scalatest.funsuite.AnyFunSuite


class ParseChoiceTests extends AnyFunSuite with ParseChoices {
  test("parseChoices") {
    object S extends DummySayables {
      val `do one thing`, `do something else` = speak
    }

    val ivrChoices = new IvrChoices(S)
    import ivrChoices._

    val choices =
      List(
        Choice.Assigned(DTMF.`1`, S.`do one thing`, ()),
        Choice.Assigned(DTMF.`2`, S.`do something else`, ())
      )

    val choiceMenu = ChoiceMenu(Sayable.Empty, choices)
    val pauseMs = 750
    val labelFirstParsed = parseChoices(choiceMenuSayable(choiceMenu, pauseMs, SayChoice.LabelFirst))
    println(">>")
    val labelLastParsed = parseChoices(choiceMenuSayable(choiceMenu, pauseMs, SayChoice.LabelLast))
    println("<<")

    assert(labelFirstParsed == labelLastParsed)

    assert(labelFirstParsed == choices.map(c => c.dtmf -> c.label))
  }
}
