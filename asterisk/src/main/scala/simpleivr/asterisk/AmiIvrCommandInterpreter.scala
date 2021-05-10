package simpleivr.asterisk

import cats.effect.IO
import simpleivr.IOIvrCommandInterpreter


trait AmiIvrCommandInterpreter extends IOIvrCommandInterpreter {
  def ami: Ami

  override def originate(dest: String, script: String, args: Seq[String]): IO[Unit] =
    IO.blocking {
      ami.originate(dest, script, args)
    }
}
