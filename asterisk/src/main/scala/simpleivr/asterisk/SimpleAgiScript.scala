package simpleivr.asterisk

import org.asteriskjava.fastagi._
import simpleivr._


abstract class SimpleAgiScript(speakGenerator: SpeakGenerator = Text2waveSpeakGenerator) extends AgiScript {
  abstract class Handler(request: AgiRequest, channel: AgiChannel) {
    protected def mkApi: IvrApi = new AgiIvrApi(channel)
    lazy val api = mkApi

    protected def ivrCommandInterpreter = new DefaultIvrCommandInterpreter(api, speakGenerator)

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
