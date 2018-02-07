package simpleivr

import java.io.File

import cats.effect.IO


class IvrCommandInterpreter(ivrApi: IvrApi, speakGenerator: SpeakGenerator = Text2waveSpeakGenerator)
  extends IvrCommand.Folder[IO] {
  /**
    * `None` if no DTMF was received, otherwise `Some(d)` where `d` is the
    * digit that was pressed.
    */
  final def runSayable(sayable: Sayable, interrupt: String): IO[Option[Char]] = sayable match {
    case SayNothing =>
      IO.pure(None)

    case Pause(ms) =>
      if (interrupt.nonEmpty)
        waitForDigit(ms)
      else
        IO(Thread.sleep(ms)).map(_ => None)

    case play: Play =>
      streamFile(play.path.pathAndName, interrupt).map {
        case 0 => None
        case c => Some(c)
      }

    case speak: Sayables#Speak =>
      speakGenerator(speak)
        .flatMap { _ =>
        println("Speaking: " + speak.msg)
        runSayable(Play(speak.path), interrupt)
      }

    case SayableSeq(messages) =>
      def loop(sayables: List[Sayable]): IO[Option[Char]] = sayables match {
        case Nil         => IO.pure(None)
        case msg :: msgs =>
          runSayable(msg, interrupt).flatMap {
            case Some(c) => IO.pure(Some(c))
            case None    => loop(msgs)
          }
      }

      loop(messages)
  }
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
  override def say(sayable: Sayable, interruptDigits: String) = runSayable(sayable, interruptDigits)
  override def originate(dest: String, script: String, args: Seq[String]) = IO {
    ivrApi.originate(dest, script, args)
  }
  override def liftIO[A](io: IO[A]) = io
}
