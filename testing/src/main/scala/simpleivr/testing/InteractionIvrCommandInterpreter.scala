package simpleivr.testing

import simpleivr.{DTMF, Pause, Sayable}


class InteractionIvrCommandInterpreter(var interactions: List[Interaction], override val callerId: String = null)
                                      (checkIgnored: Int => Unit) extends NullIvrCommandInterpreter {
  private[this] var ignoredCount = 0

  private def modHead[A](pf: PartialFunction[Interaction, (A, Option[Interaction])]): Option[A] = {
    interactions.headOption match {
      case Some(Interaction.HangUp) => throw new Interaction.HangUpException
      case _                        =>
    }

    val ret =
      interactions match {
        case Nil      =>
          ignoredCount += 1
          None
        case hd :: tl =>
          pf.lift(hd) match {
            case None                =>
              ignoredCount += 1
              None
            case Some((a, maybeAct)) =>
              ignoredCount = 0
              interactions = maybeAct.fold(tl)(_ :: tl)
              Some(a)
          }
      }

    checkIgnored(ignoredCount)

    ret
  }

  private def takeInput(allowed: DTMF => Boolean = _ => true): PartialFunction[Interaction, (Option[DTMF], Option[Interaction])] = {
    case Interaction.Press(hd :: tl) if allowed(hd) => Some(hd) -> Some(Interaction.Press(tl)).filter(_.dtmfs.nonEmpty)
    case Interaction.Press(Nil)                     => None -> None
  }

  private object digit {
    def unapply(sayable: Sayable): Option[DTMF] = sayable match {
      case Sayable("one")   => Some(DTMF.`1`)
      case Sayable("two")   => Some(DTMF.`2`)
      case Sayable("three") => Some(DTMF.`3`)
      case Sayable("four")  => Some(DTMF.`4`)
      case Sayable("five")  => Some(DTMF.`5`)
      case Sayable("six")   => Some(DTMF.`6`)
      case Sayable("seven") => Some(DTMF.`7`)
      case Sayable("eight") => Some(DTMF.`8`)
      case Sayable("nine")  => Some(DTMF.`9`)
      case Sayable("zero")  => Some(DTMF.`0`)
      case Sayable("pound") => Some(DTMF.`#`)
      case Sayable("star")  => Some(DTMF.*)
    }
  }

  private def toChoices(sayables: List[Sayable.Single]): List[(DTMF, Sayable)] =
    Util.spans(sayables) { case Pause(_) => true case _ => false }
      .flatMap {
        case Sayable("Press") +: digit(d) +: label => Some(d -> Sayable.Seq(label))
        case label :+ Sayable("Press") :+ digit(d) => Some(d -> Sayable.Seq(label))
        case other                                 => None
      }

  override def waitForDigit(timeout: Int): Option[DTMF] = modHead(takeInput()).flatten
  override def say(sayable: Sayable, interruptDtmfs: Set[DTMF]): Option[DTMF] = {
    object choice {
      def unapply(text: String): Option[DTMF] =
        toChoices(sayable.toSingles.toList)
          .find(_._2.toString().contains(text))
          .map(_._1)
    }
    modHead {
      case Interaction.WaitForSayable(f) if f(sayable) => () -> None
      case Interaction.ChooseOption(choice(ch))        => () -> Some(Interaction.Press(List(ch)))
    }
    modHead(takeInput(interruptDtmfs)).flatten
  }
  override def originate(dest: String, script: String, args: Seq[String]): Unit =
    modHead { case Interaction.WaitForOriginate(f) if f(dest, script, args) => () -> None }
  override def dial(to: String, ringTimeout: Int, flags: String): Int = {
    modHead { case Interaction.WaitForDial(f) if f(to) => () -> None }
    0
  }
}


