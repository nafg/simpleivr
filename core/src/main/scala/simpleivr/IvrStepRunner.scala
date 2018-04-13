package simpleivr

import cats.effect.IO


class IvrStepRunner(commandInterpreter: IvrCommand.Interpreter[IO]) {
  def runIvrCommand[A](cmd: IvrCommandF[A]): IO[A] = cmd.fold[IO](commandInterpreter)
  def runIvrStep[A](step: IvrStep[A]): IO[A] = step.runM(runIvrCommand)
}
