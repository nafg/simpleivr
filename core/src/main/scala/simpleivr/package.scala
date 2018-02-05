import java.io.File

import cats.Functor
import cats.free.Free


package object simpleivr {
  type IvrStep[A] = Free[IvrCommandF, A]

  object IvrStep extends IvrCommand.Folder[IvrStep] {
    def apply[A](result: A): IvrStep[A] = Free.pure(result)
    def unit = apply(())
    override def default[T] = _.ivrStep
  }

  private trait Cont[A, R] {
    type L[I] = (I => IvrStep[A]) => R
  }
  type FoldCmd[A, R] = IvrCommand.Folder[Cont[A, R]#L]

  implicit class IvrStepExtensionMethods[A](private val self: IvrStep[A]) {
    def get: Option[A] = self.fold(Some(_), _ => None)

    final def next: Either[IvrStep[A], A] = {
      val fwd = self.step
      fwd.fold(Right(_), x => Left(fwd))
    }

    private def contFunctor[R]: Functor[Cont[A, R]#L] = new Functor[Cont[A, R]#L] {
      override def map[T, U](ft: Cont[A, R]#L[T])(f: T => U) = { k =>
        ft(t => k(f(t)))
      }
    }

    def runNext[R](folder: FoldCmd[A, R]): Either[R, A] =
      self.resume.left.map(_.fold[Cont[A, R]#L](folder)(contFunctor).apply(identity))

    private def throwMatchErr: IvrCommand[_] => Any => Nothing = cmd => _ => throw new MatchError(cmd)
    def foldNext[R](streamFileF: IvrCommand.StreamFile => (Char => IvrStep[A]) => R = throwMatchErr,
                    recordFileF: IvrCommand.RecordFile => (Char => IvrStep[A]) => R = throwMatchErr,
                    waitForDigitF: IvrCommand.WaitForDigit => (Option[Char] => IvrStep[A]) => R = throwMatchErr,
                    dialF: IvrCommand.Dial => (Int => IvrStep[A]) => R = throwMatchErr,
                    amdF: (Int => IvrStep[A]) => R = throwMatchErr(IvrCommand.Amd),
                    getVarF: IvrCommand.GetVar => (Option[String] => IvrStep[A]) => R = throwMatchErr,
                    callerIdF: (String => IvrStep[A]) => R = throwMatchErr(IvrCommand.CallerId),
                    waitForSilenceF: IvrCommand.WaitForSilence => (() => IvrStep[A]) => R = throwMatchErr,
                    monitorF: IvrCommand.Monitor => (() => IvrStep[A]) => R = throwMatchErr,
                    hangupF: (() => IvrStep[A]) => R = throwMatchErr(IvrCommand.Hangup),
                    setAutoHangupF: IvrCommand.SetAutoHangup => (() => IvrStep[A]) => R = throwMatchErr,
                    sayF: IvrCommand.Say => (Option[Char] => IvrStep[A]) => R = throwMatchErr,
                    originateF: IvrCommand.Originate => (() => IvrStep[A]) => R = throwMatchErr,
                   ): Either[R, A] = runNext(new FoldCmd[A, R] {
      override def streamFile(pathAndName: String, interruptChars: String) =
        streamFileF(IvrCommand.StreamFile(pathAndName, interruptChars))
      override def recordFile(pathAndName: String, format: String, interruptChars: String, timeLimitMillis: Int, offset: Int, beep: Boolean, maxSilenceSecs: Int) =
        recordFileF(IvrCommand.RecordFile(pathAndName, format, interruptChars, timeLimitMillis, offset, beep, maxSilenceSecs))
      override def waitForDigit(timeout: Int) = waitForDigitF(IvrCommand.WaitForDigit(timeout))
      override def dial(to: String, ringTimeout: Int, flags: String) = dialF(IvrCommand.Dial(to, ringTimeout, flags))
      override def amd = amdF
      override def getVar(name: String) = getVarF(IvrCommand.GetVar(name))
      override def callerId = callerIdF
      override def waitForSilence(ms: Int, repeat: Int, timeoutSec: Option[Int]) = f => waitForSilenceF(IvrCommand.WaitForSilence(ms, repeat, timeoutSec))(() => f(()))
      override def monitor(file: File) = f => monitorF(IvrCommand.Monitor(file))(() => f(()))
      override def hangup = f => hangupF(() => f(()))
      override def setAutoHangup(seconds: Int) = f => setAutoHangupF(IvrCommand.SetAutoHangup(seconds))(() => f(()))
      override def say(sayable: Sayable, interruptDigits: String) = sayF(IvrCommand.Say(sayable, interruptDigits))
      override def originate(dest: String, script: String, args: Seq[String]) = f => originateF(IvrCommand.Originate(dest, script, args))(() => f(()))
    })
  }

  implicit class IvrStepOptionExtensionMethods[A](private val self: IvrStep[Option[A]]) extends AnyVal {
    final def flatMapOpt[B](none: => B = ())(some: A => IvrStep[B]) =
      self.flatMap(_.fold[IvrStep[B]](IvrStep(none))(some))
  }
}
