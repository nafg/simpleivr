package simpleivr.asterisk

import java.io.File
import java.nio.file.Paths

import cats.effect.IO
import cats.implicits._
import org.asteriskjava.fastagi.{AgiChannel, AgiHangupException}
import simpleivr.{DTMF, IOIvrCommandInterpreter}


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

  override def waitForDigit(timeout: Int): IO[Option[DTMF]] =
    IO {
      channel.waitForDigit(timeout)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case c: Char if c > 0 => IO.pure(Some(DTMF.fromChar(c)))
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
                          interruptDtmfs: Set[DTMF],
                          timeLimitMillis: Int,
                          offset: Int,
                          beep: Boolean,
                          maxSilenceSecs: Int) = IO {
    val parent = Paths.get(pathAndName).getParent.toString
    channel.exec("System", s"mkdir -p $parent")
    val ch =
      channel.recordFile(pathAndName, format, interruptDtmfs.mkString, timeLimitMillis, offset, beep, maxSilenceSecs)
    DTMF.fromChar.get(ch)
  }

  override def setAutoHangup(seconds: Int): IO[Unit] = IO {
    channel.setAutoHangup(seconds)
  }

  override def streamFile(pathAndName: String, interruptDtmfs: Set[DTMF]) =
    IO {
      channel.streamFile(pathAndName, interruptDtmfs.mkString)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case 0                => IO.pure(None)
        case c                => IO.pure(Some(DTMF.fromChar(c)))
      }
}

