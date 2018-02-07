package simpleivr

import cats.effect.IO


trait SpeakGenerator extends (Sayables#Speak => IO[Unit])
