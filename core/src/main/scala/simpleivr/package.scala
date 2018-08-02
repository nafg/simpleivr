import cats.free.Free


package object simpleivr {
  type DTMF = DTMF.Value

  type IvrStep[A] = Free[IvrCommandF, A]

  object IvrStep extends IvrCommand.Interpreter[IvrStep] {
    def apply[A](result: A): IvrStep[A] = Free.pure(result)
    def unit = apply(())
    override def default[T] = _.ivrStep
  }
}
