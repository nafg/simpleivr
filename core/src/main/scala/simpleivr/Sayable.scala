package simpleivr

import scala.language.implicitConversions

import cats.effect.IO
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
  override def toString =
    toSingles
      .collect {
        case speak: Speaks#Speak          => speak.msg
        case Pause(_)                     => "..."
        case Play(AudioPath(pathAndName)) => s"[$pathAndName]"
      }
      .mkString(" ")
}
object Sayable {
  sealed trait Single extends Sayable
  case class Seq(sayables: scala.Seq[Sayable]) extends Sayable

  val Empty: Sayable = Seq(Nil)

  def unapplySeq(sayable: Sayable): Option[scala.Seq[Any]] = Some(sayable.toSingles.map {
    case speak: Speaks#Speak => speak.msg
    case s                   => s
  })
}

case class Pause(ms: Int) extends Sayable.Single

case class Play(path: AudioPath) extends Sayable.Single
object Play {
  def ifExists(files: AudioFiles, path: AudioPath): IO[Option[Play]] =
    files.exists.map {
      case true  => Some(Play(path))
      case false => None
    }
}

trait Speaks {
  def audioFileBackend: AudioFileBackend

  protected def Speak(msg: String) = new Speak(msg)
  object Speak {
    def unapply(s: Speak) = Some(s.msg)
  }
  class Speak private[Speaks](val msg: String) extends Sayable.Single {
    def backend: AudioFileBackend = Speaks.this.audioFileBackend
    def files = backend.speakFiles(this)
    def path = backend.speakPath(this)
    override def toString = s"Speak($msg)"
  }

  protected def speak(implicit name: Name) = Speak(name.value)

  def speaks: List[Speak] =
    getClass.getMethods.toList
      .filter(classOf[Speak] isAssignableFrom _.getReturnType)
      .filter(_.getParameterCount == 0)
      .map(_.invoke(this).asInstanceOf[Speak])
}
