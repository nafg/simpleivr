package simpleivr

import cats.free.Free
import cats.implicits._


class IvrChoices(sayables: Sayables) extends Ivr(sayables) {

  import sayables._


  def paginated[T](maximum: Int, choices: List[Choice[T]], fixedChoices: List[Choice[T]]): List[Choice[IvrStep[T]]] = {
    def doPage(page: List[Choice[T]], rest: List[Choice[T]], back: List[Choice[T]]): List[Choice[IvrStep[T]]] = {
      def askNextPage: IvrStep[T] = Free.defer {
        val (p, r) = rest.splitAt(7)
        val b = page ::: back
        askChoice(ChoiceMenu(Sayable.Empty, doPage(p, r, b))).flatten
      }

      def askPrevPage: IvrStep[T] = Free.defer {
        if (back.length >= maximum) {
          val num = if (back.length <= 8) back.length else 7
          val (page2, back2) = back.splitAt(num)
          val rest2 = page ::: rest
          askChoice(ChoiceMenu(Sayable.Empty, doPage(page2, rest2, back2))).flatten
        } else {
          val (page2, rest2) = choices.splitAt(maximum - 1)
          val back2 = Nil
          askChoice(ChoiceMenu(Sayable.Empty, doPage(page2, rest2, back2))).flatten
        }
      }

      page.map(_ map IvrStep.apply) ++
        List(Choice(DTMF.`8`, `For more choices`, askNextPage)).filter(_ => rest.nonEmpty) ++
        List(Choice(DTMF.`9`, `For the previous choices`, askPrevPage)).filter(_ => back.nonEmpty) ++
        fixedChoices.map(_ map IvrStep.apply)
    }

    doPage(choices.take(maximum - 1), choices.drop(maximum - 1), Nil)
  }

  sealed class SayChoice(val func: (DTMF, Sayable) => Sayable)
  object SayChoice {
    case object LabelFirst extends SayChoice((key, label) => label & `Press` & dtmfWord(key))
    case object LabelLast extends SayChoice((key, label) => `Press` & dtmfWord(key) & label)
  }

  def choiceMenuSayable[A](choiceMenu: ChoiceMenu[A], pauseMs: Int, sayChoice: SayChoice) =
    choiceMenu.title &
      Sayable.Many(
        choiceMenu.assigned.map {
          case Choice.Assigned(key, label, _) =>
            Sayable.Group(Pause(pauseMs) & sayChoice.func(key, label))
        }
      )

  protected def defaultAskChoicePauseMs: Int = 750
  protected def defaultAskChoiceSayChoice: SayChoice = SayChoice.LabelLast

  def askChoice[A](choiceMenu: ChoiceMenu[A],
                   pauseMs: Int = defaultAskChoicePauseMs,
                   sayChoice: SayChoice = defaultAskChoiceSayChoice): IvrStep[A] = {
    val menuSayable = choiceMenuSayable(choiceMenu, pauseMs, sayChoice)

    def loop: IvrStep[A] =
      sayAndGetDigit(menuSayable)
        .flatMap {
          case None    => IvrStep.say(`Please make a selection` & Pause(pauseMs)) *> loop
          case Some(c) =>
            choiceMenu.assigned.find(_.maybeDtmf.contains(c)) match {
              case Some(choice) => IvrStep(choice.value)
              case None         => IvrStep.say(`That is not one of the choices.` & Pause(pauseMs)) *> loop
            }
        }

    loop
  }
}
