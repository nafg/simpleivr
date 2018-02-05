package simpleivr.testing

import simpleivr.{IvrChoices, Pause, Sayable, SayableSeq, Sayables}


class TestIvr(sayables: Sayables) extends IvrChoices(sayables) {
  object Sayables {
    private def flatten(msgs: Seq[Sayable]): Seq[Sayable] = msgs.flatMap {
      case s: SayableSeq => flatten(s.messages)
      case s             => List(s)
    }

    def unapplySeq(sayable: Sayable): Option[Seq[Any]] = Some(sayable match {
      case s: SayableSeq       => flatten(s.messages) flatMap (x => unapplySeq(x).toList.flatten)
      case sayables.Speak(msg) => List(msg)
      case s                   => List(s)
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
