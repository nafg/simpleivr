package simpleivr.testing

import java.io.File

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import simpleivr.{DTMF, Sayable}


class NullIvrCommandInterpreter extends IdIvrCommandInterpreter {
  override def streamFile(pathAndName: String, interruptDtmfs: Set[DTMF]): Option[DTMF] = None
  override def waitForDigit(timeout: Int): Option[DTMF] = None
  override def say(sayable: Sayable, interruptDtmfs: Set[DTMF]): Option[DTMF] = None
  override def callerId: String = null
  override def recordFile(pathAndName: String,
                          format: String,
                          interruptDtmfs: Set[DTMF],
                          timeLimitMillis: Int,
                          offset: Int,
                          beep: Boolean,
                          maxSilenceSecs: Int): Option[DTMF] = None
  override def dial(to: String, ringTimeout: Int, flags: String): Int = 0
  override def amd: Int = 0
  override def getVar(name: String): Option[String] = None
  override def waitForSilence(ms: Int, repeat: Int, timeoutSec: Option[Int]): Unit = ()
  override def monitor(file: File): Unit = ()
  override def hangup: Unit = ()
  override def setAutoHangup(seconds: Int): Unit = ()
  override def originate(dest: String, script: String, args: Seq[String]): Unit = ()
  override def liftIO[A](io: IO[A]): A = io.unsafeRunSync()
}
