package simpleivr.testing

import simpleivr.{Pause, Sayable}


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

  private def takeInput(allowed: Char => Boolean = _ => true): PartialFunction[Interaction, (Option[Char], Option[Interaction])] = {
    case Interaction.Press(hd :: tl) if allowed(hd) => Some(hd) -> Some(Interaction.Press(tl)).filter(_.digits.nonEmpty)
    case Interaction.Press(Nil)                     => None -> None
  }

  private object digit {
    def unapply(sayable: Sayable): Option[Char] = sayable match {
      case Sayable("one")   => Some('1')
      case Sayable("two")   => Some('2')
      case Sayable("three") => Some('3')
      case Sayable("four")  => Some('4')
      case Sayable("five")  => Some('5')
      case Sayable("six")   => Some('6')
      case Sayable("seven") => Some('7')
      case Sayable("eight") => Some('8')
      case Sayable("nine")  => Some('9')
      case Sayable("zero")  => Some('0')
      case Sayable("pound") => Some('#')
      case Sayable("star")  => Some('*')
    }
  }
  private def toChoices(sayables: List[Sayable]): List[(Char, Sayable)] = sayables match {
    case Pause(_) :: Sayable("Press") :: digit(d) :: rest =>
      val (label, more) = rest.span { case Pause(_) => false case _ => true }
      (d -> Sayable.Seq(label)) :: toChoices(more)
    case _ :: rest                                        => toChoices(rest)
    case Nil                                              => Nil
  }

  override def waitForDigit(timeout: Int): Option[Char] = modHead(takeInput()).flatten
  override def say(sayable: Sayable, interruptDigits: String): Option[Char] = {
    object choice {
      def unapply(text: String) =
        toChoices(sayable.toSingles.toList)
          .find(_._2.toString().contains(text))
          .map(_._1)
    }
    modHead {
      case Interaction.WaitForSayable(f) if f(sayable) => () -> None
      case Interaction.ChooseOption(choice(ch))        => () -> Some(Interaction.Press(List(ch)))
    }
    modHead(takeInput(interruptDigits contains _)).flatten
  }
  override def originate(dest: String, script: String, args: Seq[String]): Unit =
    modHead { case Interaction.WaitForOriginate(f) if f(dest, script, args) => () -> None }
  override def dial(to: String, ringTimeout: Int, flags: String): Int = {
    modHead { case Interaction.WaitForDial(f) if f(to) => () -> None }
    0
  }
}
