package simpleivr

import cats.effect.IO
import cats.implicits._


class SpeakGeneratingSayer(interp: IvrCommand.Interpreter[IO], interruptDigits: String, speakGenerator: SpeakGenerator)
  extends DefaultSayer(interp, interruptDigits) {

  override def speak(spk: Speaks#Speak) =
    speakGenerator(spk) >>
      super.speak(spk)
}
