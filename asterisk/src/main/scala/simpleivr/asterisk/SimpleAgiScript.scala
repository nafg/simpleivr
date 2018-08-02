package simpleivr.asterisk

import cats.effect.IO
import org.asteriskjava.fastagi._
import simpleivr._


class DefaultIvrCommandInterpreter(val channel: AgiChannel, val sayer: String => Sayable => IO[Option[Char]])
  extends SayIvrCommandInterpreter with AgiIvrCommandInterpreter

abstract class SimpleAgiScript extends AgiScript {
  abstract class Handler(val request: AgiRequest, val channel: AgiChannel) {
    def sayer: String => Sayable => IO[Option[Char]] =
      digits => new DefaultSayer(ivrCommandInterpreter, digits)

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
