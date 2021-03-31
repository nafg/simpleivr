package simpleivr.testing

import simpleivr.Sayable.{Group, Many, Single}
import simpleivr.{DTMF, Pause, Sayable}


trait ParseChoices {
  private object digit {
    def unapply(message: String): Option[DTMF] = message match {
      case "one"   => Some(DTMF.`1`)
      case "two"   => Some(DTMF.`2`)
      case "three" => Some(DTMF.`3`)
      case "four"  => Some(DTMF.`4`)
      case "five"  => Some(DTMF.`5`)
      case "six"   => Some(DTMF.`6`)
      case "seven" => Some(DTMF.`7`)
      case "eight" => Some(DTMF.`8`)
      case "nine"  => Some(DTMF.`9`)
      case "zero"  => Some(DTMF.`0`)
      case "pound" => Some(DTMF.`#`)
      case "star"  => Some(DTMF.*)
    }
  }

  def parseChoices(sayable: Sayable): List[(DTMF, Sayable)] =
    sayable match {
      case _: Single                                                        => Nil
      case Many(Pause(_) +: Sayable("Press") +: Sayable(digit(d)) +: label) => List(d -> Sayable(label))
      case Many(Pause(_) +: label :+ Sayable("Press") :+ Sayable(digit(d))) => List(d -> Sayable(label))
      case Many(sayables)                                                   => sayables.flatMap(parseChoices).toList
      case Group(s)                                                         => parseChoices(s)
    }
}
