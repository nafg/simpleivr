package simpleivr

import cats.effect.IO


trait SayIvrCommandInterpreter extends IvrCommand.Interpreter[IO] {
  def sayer: String => Sayable => IO[Option[Char]]

  override def say(sayable: Sayable, interruptDigits: String) =
    sayer(interruptDigits).apply(sayable)
}
