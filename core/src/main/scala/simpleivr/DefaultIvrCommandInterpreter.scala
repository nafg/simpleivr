package simpleivr

import java.io.File

import cats.effect.IO


class DefaultIvrCommandInterpreter(ivrApi: IvrApi, val speakGenerator: SpeakGenerator = Text2waveSpeakGenerator)
  extends SayIvrCommandInterpreter {

  override def streamFile(pathAndName: String, interruptChars: String) = IO {
    ivrApi.streamFile(pathAndName, interruptChars)
  }
  override def recordFile(pathAndName: String, format: String, interruptChars: String, timeLimitMillis: Int, offset: Int, beep: Boolean, maxSilenceSecs: Int) = IO {
    ivrApi.recordFile(pathAndName, format, interruptChars, timeLimitMillis, offset, beep, maxSilenceSecs)
  }
  override def waitForDigit(timeout: Int) = ivrApi.waitForDigit(timeout)
  override def dial(to: String, ringTimeout: Int, flags: String) = IO {
    ivrApi.dial(to, ringTimeout, flags)
  }
  override def amd = IO {
    ivrApi.amd()
  }
  override def getVar(name: String) = IO {
    ivrApi.getVar(name)
  }
  override def callerId = ivrApi.callerId
  override def waitForSilence(ms: Int, repeat: Int, timeoutSec: Option[Int]) = IO {
    ivrApi.waitForSilence(ms, repeat, timeoutSec)
  }
  override def monitor(file: File) = IO {
    ivrApi.monitor(file)
  }
  override def hangup = IO {
    ivrApi.hangup()
  }
  override def setAutoHangup(seconds: Int) = ivrApi.setAutoHangup(seconds)
  override def originate(dest: String, script: String, args: Seq[String]) = IO {
    ivrApi.originate(dest, script, args)
  }
  override def liftIO[A](io: IO[A]) = io
}
