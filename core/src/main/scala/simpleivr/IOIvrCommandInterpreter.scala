package simpleivr

import cats.effect.IO


trait IOIvrCommandInterpreter extends IvrCommand.Interpreter[IO] {
  override def default[T]: IvrCommand[T] => IO[T] = (cmd: IvrCommand[T]) => IO.raiseError(new MatchError(cmd))

  override def liftIO[A](io: IO[A]) = io
}
