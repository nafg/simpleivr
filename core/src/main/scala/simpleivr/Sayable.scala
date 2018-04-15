package simpleivr

import scala.language.implicitConversions

import sourcecode.Name


sealed trait Sayable {
  final def &(that: Sayable) = (this, that) match {
    case (Sayable.Empty, _)                       => that
    case (_, Sayable.Empty)                       => this
    case (Sayable.Seq(msgs1), Sayable.Seq(msgs2)) => Sayable.Seq(msgs1 ++ msgs2)
    case (Sayable.Seq(msgs1), msg2)               => Sayable.Seq(msgs1 :+ msg2)
    case (msg1, Sayable.Seq(msgs2))               => Sayable.Seq(msg1 +: msgs2)
    case (msg1, msg2)                             => Sayable.Seq(List(msg1, msg2))
  }
  final def toSingles: Seq[Sayable.Single] = this match {
    case single: Sayable.Single => Seq(single)
    case Sayable.Seq(sayables)  => sayables.flatMap(_.toSingles)
  }
}
object Sayable {
  sealed trait Single extends Sayable
  case class Seq(sayables: List[Sayable]) extends Sayable

  val Empty: Sayable = Seq(Nil)

  implicit def fromSeqSayable(s: scala.Seq[Sayable]): Sayable = Sayable.Seq(s.toList)
}

case class Pause(ms: Int) extends Sayable.Single

case class Play(path: AudioPath) extends Sayable.Single
object Play {
  def ifExists(files: AudioFiles, path: AudioPath): Option[Play] =
    if (files.exists()) Some(Play(path))
    else None
}

trait Speaks {
  def base: AudioBase

  protected def Speak(msg: String) = new Speak(msg)
  object Speak {
    def unapply(s: Speak) = Some(s.msg)
  }
  class Speak private[Speaks](val msg: String) extends Sayable.Single {
    lazy val filename = msg.replaceAll("\\W", "-").trim.toLowerCase
    lazy val path = AudioPath(base.path, filename)
    lazy val files = AudioFiles(base.localDir, filename)
    override def toString = s"Speak($msg)"
  }

  protected def speak(implicit name: Name) = Speak(name.value)

  def speaks: List[Speak] =
    getClass.getMethods.toList
      .filter(classOf[Speak] isAssignableFrom _.getReturnType)
      .filter(_.getParameterCount == 0)
      .map(_.invoke(this).asInstanceOf[Speak])
}
