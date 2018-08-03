package simpleivr

sealed trait Choice[+A] {
  def maybeDtmf: Option[DTMF]
  def label: Sayable
  def value: A
  def map[B](f: A => B): Choice[B]
}

object Choice {
  case class Unassigned[+A](label: Sayable, value: A) extends Choice[A] {
    override def maybeDtmf = None
    override def map[B](f: A => B) = copy(value = f(value))
  }
  case class Assigned[+A](dtmf: DTMF, label: Sayable, value: A) extends Choice[A] {
    override def maybeDtmf = Some(dtmf)
    override def map[B](f: A => B) = copy(value = f(value))
  }
  def apply[A](label: Sayable, value: A): Choice[A] = Unassigned(label, value)
  def apply[A](dtmf: DTMF, label: Sayable, value: A): Choice[A] = Assigned(dtmf, label, value)

  def unapply[A](choice: Choice[A]) = Some((choice.maybeDtmf, choice.label, choice.value))
}
