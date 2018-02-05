package simpleivr

import cats.implicits._


class Ivr(sayables: Sayables) {

  import sayables._


  def record(desc: Sayable, path: AudioPath, timeLimitInSeconds: Int): IvrStep[Unit] =
    (IvrStep.say(`Please say` & desc & `after the tone, and press pound when finished.`) *>
      IvrStep.recordFile(path.pathAndName, "wav", "#", timeLimitInSeconds * 1000, 0, beep = true, 3))
      .void

  def confirmRecording(desc: Sayable, file: Sayable): IvrStep[Option[Boolean]] =
    askYesNo(desc & `is` & file & `Is that correct?`)

  def sayAndGetDigit(msgs: Sayable, wait: Int = 5000): IvrStep[Option[Char]] =
    IvrStep.say(msgs, "0123456789#*").flatMap {
      case Some(c) => IvrStep(Some(c))
      case None    => IvrStep.waitForDigit(wait)
    }

  /**
    * None means * was pressed, signifying that inputting was canceled
    */
  def sayAndHandleDigits[T](min: Int, max: Int, msgs: Sayable)
                           (handle: PartialFunction[String, T] = PartialFunction(identity[String])): IvrStep[Option[T]] = {
    def validate(acc: String): Either[Sayable, T] =
      if ((min == max) && (acc.length != min))
        Left(`You must enter ` & numberWords(min) & (if (min == 1) `digit` else `digits`))
      else if (acc.length < min)
        Left(`You must enter at least` & numberWords(min) & (if (min == 1) `digit` else `digits`))
      else if (acc.length > max)
        Left(`You cannot enter more than` & numberWords(max) & (if (max == 1) `digit` else `digits`))
      else
        handle.andThen(Right(_)).applyOrElse(acc, (_: String) => Left(`That entry is not valid`))

    def calcRes(acc: String = ""): Option[Char] => IvrStep[Either[Sayable, Option[T]]] = {
      case Some(c) if acc.length + 1 < max && c.isDigit =>
        IvrStep.waitForDigit(5000).flatMap(calcRes(acc + c))
      case Some('*')                                    =>
        IvrStep(Right(None))
      case x                                            =>
        val s = acc + x.filter(_ != '#').mkString
        IvrStep(validate(s).right.map(Option(_)))
    }

    sayAndGetDigit(msgs)
      .flatMap(calcRes())
      .flatMap {
        case Right(x)  => IvrStep(x)
        case Left(msg) => IvrStep.say(msg) *> sayAndHandleDigits(min, max, msgs)(handle)
      }
  }

  def askYesNo(msgs: Sayable): IvrStep[Option[Boolean]] =
    sayAndGetDigit(msgs & `Press 1 for yes, or 2 for no.`) flatMap {
      case Some('1') => IvrStep(Some(true))
      case Some('2') => IvrStep(Some(false))
      case Some('*') => IvrStep(None)
      case None      => IvrStep.say(`Please make a selection`) *> askYesNo(msgs)
      case _         => IvrStep.say(`That is not one of the choices.`) *> askYesNo(msgs)
    }
}
