package simpleivr.asterisk

import java.io.File
import java.nio.file.Paths

import cats.effect.IO
import cats.implicits._
import org.asteriskjava.fastagi.{AgiChannel, AgiHangupException}
import simpleivr.IOIvrCommandInterpreter


/**
  * Implements an IvrCommand.Interpreter for Asterisk AGI
  */
trait AgiIvrCommandInterpreter extends IOIvrCommandInterpreter {
  def channel: AgiChannel

  final val HangupReturnCode = -1.toChar

  def hangupAndQuit: IO[Nothing] =
    hangup *>
      IO.raiseError(new AgiHangupException)

  override def dial(to: String, ringTimeout: Int, flags: String) = IO {
    channel.exec("Dial", s"$to,$ringTimeout|$flags")
  }

  override def amd = IO {
    channel.exec("AMD")
  }

  override def getVar(name: String) = IO {
    Option(channel.getFullVariable("${" + name + "}"))
  }

  override def callerId: IO[String] = IO {
    channel.getFullVariable("$" + "{CALLERID(num)}")
  }

  override def waitForDigit(timeout: Int): IO[Option[Char]] =
    IO {
      channel.waitForDigit(timeout)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case c: Char if c > 0 => IO.pure(Some(c))
        case _                => IO.pure(None)
      }


  override def waitForSilence(ms: Int, repeat: Int = 1, timeoutSec: Option[Int] = None) = IO {
    channel.exec("WaitForSilence", s"$ms,$repeat" + timeoutSec.map("," + _).getOrElse(""))
  }

  override def monitor(file: File) = IO {
    channel.exec("System", s"mkdir -p ${file.getParentFile.getAbsolutePath}")
    channel.exec("MixMonitor", file.getAbsolutePath)
  }

  override def hangup = IO {
    channel.hangup()
  }

  override def recordFile(pathAndName: String,
                          format: String,
                          interruptChars: String,
                          timeLimitMillis: Int,
                          offset: Int,
                          beep: Boolean,
                          maxSilenceSecs: Int) = IO {
    val parent = Paths.get(pathAndName).getParent.toString
    channel.exec("System", s"mkdir -p $parent")
    channel.recordFile(pathAndName, format, interruptChars, timeLimitMillis, offset, beep, maxSilenceSecs)
  }

  override def setAutoHangup(seconds: Int): IO[Unit] = IO {
    channel.setAutoHangup(seconds)
  }

  override def streamFile(pathAndName: String, interruptChars: String) =
    IO {
      channel.streamFile(pathAndName, interruptChars)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case c                => IO.pure(c)
      }
}

