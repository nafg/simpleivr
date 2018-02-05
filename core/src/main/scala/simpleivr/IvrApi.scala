package simpleivr

import java.io.File

import cats.effect.IO


trait IvrApi {
  def streamFile(pathAndName: String, interruptChars: String): Char
  def recordFile(pathAndName: String, format: String, interruptChars: String, timeLimitMillis: Int, offset: Int, beep: Boolean, maxSilenceSecs: Int): Char
  def waitForDigit(timeout: Int): IO[Option[Char]]
  def dial(to: String, ringTimeout: Int, flags: String): Int
  def amd(): Int
  def getVar(name: String): Option[String]
  def callerId: IO[String]
  def waitForSilence(ms: Int, repeat: Int = 1, timeoutSec: Option[Int] = None): Unit
  def monitor(file: File): Unit
  def hangup(): Unit
  def setAutoHangup(seconds: Int): IO[Unit]
  def originate(dest: String, script: String, args: Seq[String])
}
