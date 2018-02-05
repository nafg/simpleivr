package simpleivr.asterisk

import org.asteriskjava.fastagi._
import simpleivr.{IvrApi, IvrStep, IvrStepRunner, Sayables}


abstract class DefaultAgiScript(sayables: Sayables, api: IvrApi) extends AgiScript {
  def run(request: AgiRequest): IvrStep[Unit]

  def ivrStepRunner(request: AgiRequest) = new IvrStepRunner(api, sayables)

  override def service(request: AgiRequest, channel: AgiChannel): Unit = {
    channel.answer()
    try {
      val runner = ivrStepRunner(request)
      runner.runIvrStep(run(request)).unsafeRunSync()
      channel.hangup()
    } catch {
      case e: AgiHangupException =>
        println("Caught hangup")
    }
  }
}
