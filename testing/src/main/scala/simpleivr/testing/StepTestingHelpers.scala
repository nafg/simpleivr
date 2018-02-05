package simpleivr.testing

import org.scalatest.exceptions.TestFailedException
import simpleivr.{FoldCmd, IvrStep, Sayable}


object StepTestingHelpers {
  implicit class mustMatchOps[A](a: A) {
    /**
      * @example
      * {{{
      *   Person("jack", 50) mustMatch {
      *     case Person(_, age) => age shouldBe 50
      *   }
      * }}}
      */
    def mustMatch[R](pf: PartialFunction[A, R]): R =
      pf.applyOrElse[A, R](a, _ => throw new TestFailedException(s"Unexpected value: $a", 6))
  }
  implicit class nextStepEitherOps[A, B](e: Either[A, B]) {
    def next[R](f: A => R): R = e mustMatch {
      case Left(x) => f(x)
    }
  }

  type InpCont[A] = Option[Char] => IvrStep[A]

  implicit class pressCharOp[R](f: InpCont[R]) {
    def press(presses: Seq[Char]) = presses match {
      case Seq()                 => Left(f(None))
      case Seq(oneChar)          => Left(f(Some(oneChar)))
      case Seq(first, rest @ _*) => f(Some(first)) press rest
    }
    def press(c: Char) = if (c == ',') f(None) else f(Some(c))
    def nopress = f(None)
  }

  implicit class pressSeqOp[A](step: IvrStep[A]) {
    private def folder(p: Option[Char], rest: List[Char]) = new FoldCmd[A, Either[IvrStep[A], A]] {
      override def default[T] = _ => _ => Left(step)
      override def say(sayable: Sayable, interruptDigits: String) = _(p).press(rest)
      override def waitForDigit(timeout: Int) = _(p).press(rest)
    }
    def press(presses: Seq[Char]): Either[IvrStep[A], A] = presses.toList match {
      case Nil         => step.next
      case ',' :: rest => step.runNext(folder(None, rest)).joinLeft
      case c :: rest   => step.runNext(folder(Some(c), rest)).joinLeft
    }
  }
}
