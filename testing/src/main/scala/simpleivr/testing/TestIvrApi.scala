package simpleivr.testing

import java.io.File

import cats.effect.IO
import simpleivr.IvrApi


object TestIvrApi extends IvrApi {
  def streamFile(pathAndName: String, interruptChars: String): Char = 0
  def waitForDigit(timeout: Int): IO[Option[Char]] = IO.pure(None)
  def recordFile(pathAndName: String, format: String, interruptChars: String, timeLimitMillis: Int, offset: Int, beep: Boolean, maxSilenceSecs: Int): Char = 0
  def dial(to: String, ringTimeout: Int, flags: String): Int = 0
  def amd(): Int = 0
  def getVar(name: String): Option[String] = None
  def callerId: IO[String] = IO.pure("")
  def waitForSilence(ms: Int, repeat: Int = 1, timeoutSec: Option[Int] = None): Unit = {}
  def monitor(file: File): Unit = {}
  def hangup(): Unit = {}
  def setAutoHangup(seconds: Int): IO[Unit] = IO.pure(())
  def originate(dest: String, script: String, args: Seq[String]): Unit = ()
}
