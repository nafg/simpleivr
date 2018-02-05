package simpleivr.asterisk

import java.io.File

import cats.effect.IO
import org.asteriskjava.fastagi.{AgiChannel, AgiHangupException}
import simpleivr.IvrApi


/**
  * Implements the IvrApi trait for Asterisk AGI
  */
class AgiIvrApi(channel: AgiChannel, val ami: Ami) extends IvrApi {
  final val HangupReturnCode = -1.toChar

  def hangupAndQuit(): Nothing = {
    hangup()
    throw new AgiHangupException
  }

  def dial(to: String, ringTimeout: Int, flags: String): Int = channel.exec("Dial", s"$to,$ringTimeout|$flags")

  def amd() = channel.exec("AMD")

  def getVar(name: String) = Option(channel.getFullVariable("${" + name + "}"))

  def callerId: IO[String] = IO {
    channel.getFullVariable("$" + "{CALLERID(num)}")
  }

  def waitForDigit(timeout: Int): IO[Option[Char]] = IO {
    channel.waitForDigit(timeout) match {
      case HangupReturnCode => hangupAndQuit()
      case c: Char if c > 0 => Some(c)
      case _                => None
    }
  }

  def waitForSilence(ms: Int, repeat: Int = 1, timeoutSec: Option[Int] = None): Unit = {
    channel.exec("WaitForSilence", s"$ms,$repeat" + timeoutSec.map("," + _).getOrElse(""))
    ()
  }

  def monitor(file: File): Unit = {
    channel.exec("MixMonitor", file.getAbsolutePath)
    ()
  }

  def hangup(): Unit = channel.hangup()

  def recordFile(pathAndName: String,
                 format: String,
                 interruptChars: String,
                 timeLimitMillis: Int,
                 offset: Int,
                 beep: Boolean,
                 maxSilenceSecs: Int): Char =
    channel.recordFile(pathAndName, format, interruptChars, timeLimitMillis, offset, beep, maxSilenceSecs)

  def setAutoHangup(seconds: Int): IO[Unit] = IO {
    channel.setAutoHangup(seconds)
  }

  def streamFile(pathAndName: String, interruptChars: String): Char =
    channel.streamFile(pathAndName, interruptChars) match {
      case HangupReturnCode => hangupAndQuit()
      case c                => c
    }

  override def originate(dest: String, script: String, args: Seq[String]): Unit =
    ami.originate(dest, script, args)
}
