package simpleivr.testing

import simpleivr.{IvrChoices, Pause, Sayable, Sayables}


class TestIvr(sayables: Sayables) extends IvrChoices(sayables) {
  object Sayables {
    def unapplySeq(sayable: Sayable): Option[Seq[Any]] = Some(sayable.toSingles.map {
      case sayables.Speak(msg) => msg
      case s                   => s
    })
  }

  def getChoices(in: Seq[Any]): Seq[(Char, Seq[String])] = {
    def readStrings(xs: List[Any], acc: List[String]): (List[Any], List[String]) = xs match {
      case (s: String) :: rest => readStrings(rest, acc :+ s)
      case _                   => (xs, acc)
    }

    object DigitWord {
      def unapply(s: String) = s match {
        case "one" => Some('1')
        case "two" => Some('2')
        case _     => None
      }
    }
    def extract(xs: List[Any], cur: Seq[(Char, Seq[String])]): Seq[(Char, Seq[String])] = xs match {
      case Pause(_) :: "Press" :: ys => extract(ys, cur)
      case DigitWord(d) :: ys        =>
        readStrings(ys, Nil) match {
          case (Nil, strings)  => cur :+ ((d, strings))
          case (rest, strings) => extract(rest, cur :+ ((d, strings)))
        }
      case _                         => cur
    }

    extract(in.toList, Nil)
  }
}
