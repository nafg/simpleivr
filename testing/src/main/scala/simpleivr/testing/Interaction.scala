package simpleivr.testing

import scala.util.matching.Regex

import simpleivr.{DTMF, Sayable}


sealed trait Interaction

object Interaction {
  case class WaitForSayable(f: Sayable => Boolean) extends Interaction
  case class Press(dtmfs: List[DTMF]) extends Interaction
  case class ChooseOption(text: String) extends Interaction
  case class WaitForOriginate(f: (String, String, Seq[String]) => Boolean) extends Interaction
  case class WaitForDial(f: String => Boolean) extends Interaction
  case object HangUp extends Interaction

  class HangUpException extends RuntimeException
}


class Interactions(val interactions: List[Interaction]) {
  def +(interaction: Interaction) = new Interactions(interaction :: interactions)

  private def contains(actual: Sayable, expected: Sayable): Boolean =
    actual.toSingles containsSlice expected.toSingles

  def waitForSayableWhere(f: Sayable => Boolean) = this + Interaction.WaitForSayable(f)
  def waitForSayableWith(sayable: Sayable) = waitForSayableWhere(contains(_, sayable))
  def waitForSayableWith(regex: Regex) = waitForSayableWhere { sayable =>
    regex.findFirstIn(sayable.toString).isDefined
  }
  def waitForSayableWith(text: String): Interactions = waitForSayableWith(Regex.quote(text).r.unanchored)
  def waitForSayableMatching(pf: PartialFunction[Sayable, Unit]) = waitForSayableWhere(pf.lift.andThen(_.isDefined))
  def press(dtmfs: String) = this + Interaction.Press(dtmfs.toList.map(DTMF.fromChar(_)))
  def chooseOption(text: String) = this + Interaction.ChooseOption(text)
  def waitForOriginateWhere(f: (String, String, Seq[String]) => Boolean) = this + Interaction.WaitForOriginate(f)
  def waitForOriginateMatching(pf: PartialFunction[(String, String, Seq[String]), Unit]) =
    waitForOriginateWhere(Function.untupled(pf.lift.andThen(_.isDefined)))
  def waitForDialWhere(f: String => Boolean) = this + Interaction.WaitForDial(f)
  def hangUp = this + Interaction.HangUp
}

object Interactions extends Interactions(Nil)
