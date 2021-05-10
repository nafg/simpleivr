package simpleivr.asterisk

import java.io.File
import java.nio.file.Paths

import cats.effect.IO
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

  override def dial(to: String, ringTimeout: Int, flags: String) = IO.blocking {
    channel.exec("Dial", s"$to,$ringTimeout|$flags")
  }

  override def amd = IO.blocking {
    channel.exec("AMD")
  }

  override def getVar(name: String) = IO.blocking {
    Option(channel.getFullVariable("${" + name + "}"))
  }

  override def callerId: IO[String] = IO.blocking {
    channel.getFullVariable("$" + "{CALLERID(num)}")
  }

  override def waitForDigit(timeout: Int): IO[Option[DTMF]] =
    IO.blocking {
      channel.waitForDigit(timeout)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case c                => IO.pure(DTMF.fromChar.get(c))
      }


  override def waitForSilence(ms: Int, repeat: Int = 1, timeoutSec: Option[Int] = None) = IO.blocking {
    channel.exec("WaitForSilence", s"$ms,$repeat" + timeoutSec.map("," + _).getOrElse(""))
    ()
  }

  override def monitor(file: File) = IO.blocking {
    channel.exec("System", s"mkdir -p ${file.getParentFile.getAbsolutePath}")
    channel.exec("MixMonitor", file.getAbsolutePath)
    ()
  }

  override def hangup = IO.blocking {
    channel.hangup()
  }

  override def recordFile(pathAndName: String,
                          format: String,
                          interruptDtmfs: Set[DTMF],
                          timeLimitMillis: Int,
                          offset: Int,
                          beep: Boolean,
                          maxSilenceSecs: Int) = IO.blocking {
    val parent = Paths.get(pathAndName).getParent.toString
    channel.exec("System", s"mkdir -p $parent")
    val ch =
      channel.recordFile(pathAndName, format, interruptDtmfs.mkString, timeLimitMillis, offset, beep, maxSilenceSecs)
    DTMF.fromChar.get(ch)
  }

  override def setAutoHangup(seconds: Int): IO[Unit] = IO.blocking {
    channel.setAutoHangup(seconds)
  }

  override def streamFile(pathAndName: String, interruptDtmfs: Set[DTMF]) =
    IO.blocking {
      channel.streamFile(pathAndName, interruptDtmfs.mkString)
    }
      .flatMap {
        case HangupReturnCode => hangupAndQuit
        case 0                => IO.pure(None)
        case c                => IO.pure(Some(DTMF.fromChar(c)))
      }
}
