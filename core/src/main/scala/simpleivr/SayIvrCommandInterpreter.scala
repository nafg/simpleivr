package simpleivr

import cats.effect.IO


trait SayIvrCommandInterpreter extends IvrCommand.Interpreter[IO] {
  def sayer: Set[DTMF] => Sayable => IO[Option[DTMF]]

  override def say(sayable: Sayable, interruptDtmfs: Set[DTMF]) =
    sayer(interruptDtmfs).apply(sayable)
}
