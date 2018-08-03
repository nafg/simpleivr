package simpleivr

import scala.language.implicitConversions

import cats.effect.IO
import sourcecode.Name


sealed trait Sayable {
  final def &(that: Sayable) = (this, that) match {
    case (Sayable.Empty, _)                         => that
    case (_, Sayable.Empty)                         => this
    case (Sayable.Many(msgs1), Sayable.Many(msgs2)) => Sayable.Many(msgs1 ++ msgs2)
    case (Sayable.Many(msgs1), msg2)                => Sayable.Many(msgs1 :+ msg2)
    case (msg1, Sayable.Many(msgs2))                => Sayable.Many(msg1 +: msgs2)
    case (msg1, msg2)                               => Sayable.Many(List(msg1, msg2))
  }
  final def toSingles: Seq[Sayable.Single] = this match {
    case single: Sayable.Single => Seq(single)
    case Sayable.Many(sayables) => sayables.flatMap(_.toSingles)
    case Sayable.Group(sayable) => sayable.toSingles
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
  case class Group(sayable: Sayable) extends Sayable
  case class Many(sayables: Seq[Sayable]) extends Sayable

  val Empty: Sayable = Many(Nil)

  def apply(sayables: Seq[Sayable]) = sayables match {
    case Seq()       => Empty
    case Seq(single) => single
    case many        => Many(many)
  }

  def unapplySeq(sayable: Sayable): Option[Seq[Any]] = Some(sayable.toSingles.map {
    case speak: Speaks#Speak => speak.msg
    case s                   => s
  })

  trait Folder[A] extends (Sayable => A) {
    def apply(sayable: Sayable): A = sayable match {
      case Pause(ms)       => pause(ms)
      case Play(path)      => play(path)
      case s: Speaks#Speak => speak(s)
      case Group(s)        => group(s)
      case Many(sayables)  => many(sayables.toList)
    }

    def pause(ms: Int): A
    def play(path: AudioPath): A
    def speak(spk: Speaks#Speak): A
    def group(sayable: Sayable): A
    def many(sayables: List[Sayable]): A
  }
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
