package simpleivr

import scala.language.implicitConversions

import sourcecode.Name


sealed trait Sayable {
  final def &(that: Sayable) = (this, that) match {
    case (SayNothing, _)                        => that
    case (_, SayNothing)                        => this
    case (SayableSeq(msgs1), SayableSeq(msgs2)) => SayableSeq(msgs1 ++ msgs2)
    case (SayableSeq(msgs1), msg2)              => SayableSeq(msgs1 :+ msg2)
    case (msg1, SayableSeq(msgs2))              => SayableSeq(msg1 +: msgs2)
    case (msg1, msg2)                           => SayableSeq(List(msg1, msg2))
  }
}
object Sayable {
  implicit def fromSeqSayable(s: Seq[Sayable]): Sayable = SayableSeq(s.toList)
}

object SayNothing extends Sayable

case class Pause(ms: Int) extends Sayable

case class SayableSeq(messages: List[Sayable]) extends Sayable

case class Play(path: AudioPath) extends Sayable
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
  class Speak private[Speaks](val msg: String) extends Sayable {
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
