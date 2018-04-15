package simpleivr

import cats.effect.IO


trait SayIvrCommandInterpreter extends IvrCommand.Interpreter[IO] {
  def speakGenerator: SpeakGenerator

  private def curTime = IO(System.currentTimeMillis())

  protected def runPause(ms: Int, interrupt: String): IO[Option[Char]] =
    if (ms <= 0)
      IO.pure(None)
    else if (interrupt.isEmpty)
      IO(Thread.sleep(ms)).map(_ => None)
    else
      for {
        startTime <- curTime
        digit <- waitForDigit(ms)
        res <-
          if (digit.exists(interrupt.contains(_)))
            IO.pure(digit)
          else
            curTime.map(_ - startTime).flatMap(elapsed => runPause(ms - elapsed.toInt, interrupt))
      } yield res

  /**
    * `None` if no DTMF was received, otherwise `Some(d)` where `d` is the
    * digit that was pressed.
    */
  def runSayable(sayable: Sayable, interrupt: String): IO[Option[Char]] = sayable match {
    case SayNothing =>
      IO.pure(None)

    case Pause(ms) => runPause(ms, interrupt)

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

    case Sayable.Seq(messages) =>
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

  override def say(sayable: Sayable, interruptDigits: String) = runSayable(sayable, interruptDigits)
}
