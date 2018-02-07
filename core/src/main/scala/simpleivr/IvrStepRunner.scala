package simpleivr

import cats.effect.IO


class IvrStepRunner(api: IvrApi) {
  def runIvrCommand[A](cmd: IvrCommandF[A]): IO[A] = cmd.fold[IO](new IvrCommandInterpreter(api))
  final def runIvrStep[A](step: IvrStep[A]): IO[A] = step.runM(runIvrCommand)
}
