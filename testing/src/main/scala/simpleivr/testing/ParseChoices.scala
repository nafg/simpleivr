package simpleivr.testing

import simpleivr.Sayable.{Group, Many, Single}
import simpleivr.{DTMF, Pause, Sayable}


trait ParseChoices {
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

  def parseChoices(sayable: Sayable): List[(DTMF, Sayable)] =
    sayable match {
      case _: Single                                               => Nil
      case Many(Pause(_) +: Sayable("Press") +: digit(d) +: label) => List(d -> Sayable(label))
      case Many(Pause(_) +: label :+ Sayable("Press") :+ digit(d)) => List(d -> Sayable(label))
      case Many(sayables)                                          => sayables.flatMap(parseChoices).toList
      case Group(s)                                                => parseChoices(s)
    }
}
