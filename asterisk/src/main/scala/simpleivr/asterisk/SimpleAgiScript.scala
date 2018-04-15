package simpleivr.asterisk

import cats.effect.IO
import org.asteriskjava.fastagi._
import simpleivr._


abstract class SimpleAgiScript(val speakGenerator: SpeakGenerator = Text2waveSpeakGenerator) extends AgiScript {
  abstract class Handler(val request: AgiRequest, val channel: AgiChannel) {
    trait DefaultIvrCommandInterpreter extends SayIvrCommandInterpreter with AgiIvrCommandInterpreter {
      val speakGenerator: SpeakGenerator = SimpleAgiScript.this.speakGenerator
      val channel = Handler.this.channel
    }

    protected lazy val ivrCommandInterpreter: IvrCommand.Interpreter[IO] =
      new DefaultIvrCommandInterpreter {}

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
