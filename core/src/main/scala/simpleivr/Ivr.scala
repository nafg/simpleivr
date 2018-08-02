package simpleivr

import cats.implicits._


class Ivr(sayables: Sayables) {

  import sayables._


  def loopUntilDefined[A](step: => IvrStep[Option[A]]): IvrStep[A] =
    step.flatMap {
      case None    => loopUntilDefined(step)
      case Some(a) => IvrStep(a)
    }

  def record(desc: Sayable, path: AudioPath, timeLimitInSeconds: Int): IvrStep[Unit] =
    (IvrStep.say(`Please say` & desc & `after the tone, and press pound when finished.`) *>
      IvrStep.recordFile(path.pathAndName, "wav", Set(DTMF.`#`), timeLimitInSeconds * 1000, 0, beep = true, 3))
      .void

  def confirmRecording(desc: Sayable, file: Sayable): IvrStep[Option[Boolean]] =
    askYesNo(desc & `is` & file & `Is that correct?`)

  def sayAndGetDigit(msgs: Sayable, wait: Int = 5000): IvrStep[Option[DTMF]] =
    IvrStep.say(msgs, DTMF.values).flatMap {
      case Some(c) => IvrStep(Some(c))
      case None    => IvrStep.waitForDigit(wait)
    }

  /**
    * @param handle A function, passed the accumulated previous digits and the latest digit or None if user was silent.
    *               Should return
    *               Some(Left(Sayable)) to indicate the input is invalid and should be retried,
    *               Some(Right(x)) to indicate the input is complete and the value to return,
    *               or None for input to continue.
    */
  def sayAndHandle[A](message: Sayable)
                     (handle: (String, Option[DTMF]) => Option[Either[Sayable, A]]): IvrStep[A] = {
    def calcRes(acc: String)(ch: Option[DTMF]): IvrStep[Either[Sayable, A]] =
      handle(acc, ch) match {
        case Some(res) => IvrStep(res)
        case None      => IvrStep.waitForDigit(5000).flatMap(calcRes(acc + ch.mkString))
      }

    sayAndGetDigit(message)
      .flatMap(calcRes(""))
      .flatMap {
        case Right(x)  => IvrStep(x)
        case Left(msg) => IvrStep.say(msg) *> sayAndHandle(message)(handle)
      }
  }

  /**
    * None means * was pressed, signifying that inputting was canceled
    */
  def sayAndHandleDigits[A](min: Int, max: Int, msgs: Sayable)
                           (handle: PartialFunction[String, A] = PartialFunction(identity[String])): IvrStep[Option[A]] =
    sayAndHandle(msgs) {
      case (_, Some(DTMF.*))                                   => Some(Right(None))
      case (acc, Some(c)) if acc.length + 1 < max && c.isDigit => None
      case (acc, x)                                            =>
        def sayDigitOrDigits(n: Int) = numberWords(n) & (if (n == 1) `digit` else `digits`)

        val str = acc + x.filter(_ != '#').mkString
        val validated =
          if ((min == max) && (str.length != min)) Left(`You must enter ` & sayDigitOrDigits(min))
          else if (str.length < min) Left(`You must enter at least` & sayDigitOrDigits(min))
          else if (str.length > max) Left(`You cannot enter more than` & sayDigitOrDigits(max))
          else
            handle
              .andThen(a => Right(Some(a)))
              .applyOrElse(str, (_: String) => Left(`That entry is not valid`))
        Some(validated)
    }

  def askYesNo(msgs: Sayable): IvrStep[Option[Boolean]] =
    sayAndGetDigit(msgs & `Press 1 for yes, or 2 for no.`) flatMap {
      case Some(DTMF.`1`) => IvrStep(Some(true))
      case Some(DTMF.`2`) => IvrStep(Some(false))
      case Some(DTMF.*)   => IvrStep(None)
      case None           => IvrStep.say(`Please make a selection`) *> askYesNo(msgs)
      case _              => IvrStep.say(`That is not one of the choices.`) *> askYesNo(msgs)
    }
}
