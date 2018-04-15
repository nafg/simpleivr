package simpleivr.testing

import java.io.File

import cats.effect.IO
import simpleivr.Sayable


class NullIvrCommandInterpreter extends IdIvrCommandInterpreter {
  override def streamFile(pathAndName: String, interruptChars: String): Char = 0
  override def waitForDigit(timeout: Int): Option[Char] = None
  override def say(sayable: Sayable, interruptDigits: String): Option[Char] = None
  override def callerId: String = null
  override def recordFile(pathAndName: String, format: String, interruptChars: String, timeLimitMillis: Int, offset: Int, beep: Boolean, maxSilenceSecs: Int): Char = '#'
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
