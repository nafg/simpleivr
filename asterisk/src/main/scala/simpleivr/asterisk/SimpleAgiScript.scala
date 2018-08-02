package simpleivr.asterisk

import cats.effect.IO
import org.asteriskjava.fastagi._
import simpleivr._


class DefaultIvrCommandInterpreter(val channel: AgiChannel, val sayer: Set[DTMF] => Sayable => IO[Option[DTMF]])
  extends SayIvrCommandInterpreter with AgiIvrCommandInterpreter

abstract class SimpleAgiScript extends AgiScript {
  abstract class Handler(val request: AgiRequest, val channel: AgiChannel) {
    def sayer: Set[DTMF] => Sayable => IO[Option[DTMF]] =
      dtmfs => new DefaultSayer(ivrCommandInterpreter, dtmfs)

    protected lazy val ivrCommandInterpreter: IvrCommand.Interpreter[IO] =
      new DefaultIvrCommandInterpreter(channel, sayer)

    protected def ivrStepRunner = new IvrStepRunner(ivrCommandInterpreter)

    def run: IvrStep[Unit]

    def handle(): Unit = {
      channel.answer()
      try {
        ivrStepRunner.runIvrStep(run).unsafeRunSync()
        channel.hangup()
      } catch {
        case _: AgiHangupException =>
          println("Caught hangup")
      }
    }
  }

  def handler(request: AgiRequest, channel: AgiChannel): Handler

  override def service(request: AgiRequest, channel: AgiChannel): Unit = handler(request, channel).handle()
}
