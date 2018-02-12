package simpleivr.asterisk

import org.asteriskjava.fastagi._
import simpleivr._


abstract class SimpleAgiScript(speakGenerator: SpeakGenerator = Text2waveSpeakGenerator) extends AgiScript {
  protected def makeApi(channel: AgiChannel): IvrApi = new AgiIvrApi(channel)

  protected def ivrCommandInterpreter(channel: AgiChannel) = new IvrCommandInterpreter(makeApi(channel), speakGenerator)

  protected def ivrStepRunner(channel: AgiChannel) = new IvrStepRunner(ivrCommandInterpreter(channel))

  def run(request: AgiRequest): IvrStep[Unit]

  override def service(request: AgiRequest, channel: AgiChannel): Unit = {
    channel.answer()
    try {
      val runner = ivrStepRunner(channel)
      val step = run(request)
      runner.runIvrStep(step).unsafeRunSync()
      channel.hangup()
    } catch {
      case e: AgiHangupException =>
        println("Caught hangup")
    }
  }
}
