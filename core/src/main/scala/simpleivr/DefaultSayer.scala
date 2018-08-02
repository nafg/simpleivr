package simpleivr

import cats.effect.IO


class DefaultSayer(interp: IvrCommand.Interpreter[IO], interruptDtmfs: Set[DTMF])
  extends Sayable.Folder[IO[Option[DTMF]]] {

  private def curTime = IO(System.currentTimeMillis())

  override def pause(ms: Int): IO[Option[DTMF]] =
    if (ms <= 0)
      IO.pure(None)
    else if (interruptDtmfs.isEmpty)
      IO(Thread.sleep(ms)).map(_ => None)
    else
      for {
        startTime <- curTime
        digit <- interp.waitForDigit(ms)
        res <-
          if (digit.exists(interruptDtmfs))
            IO.pure(digit)
          else
            curTime.map(_ - startTime).flatMap(elapsed => pause(ms - elapsed.toInt))
      } yield res

  override def play(path: AudioPath) = interp.streamFile(path.pathAndName, interruptDtmfs)

  override def speak(spk: Speaks#Speak) = {
    println("Speaking: " + spk.msg)
    spk.backend.speakPath(spk)
      .flatMap(play)
  }

  override def seq(sayables: List[Sayable]): IO[Option[DTMF]] =
    sayables match {
      case Nil         => IO.pure(None)
      case msg :: msgs =>
        apply(msg)
          .flatMap {
            case Some(c) => IO.pure(Some(c))
            case None    => seq(msgs)
          }
    }
}


