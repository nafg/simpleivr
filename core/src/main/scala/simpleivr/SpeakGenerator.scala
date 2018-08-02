package simpleivr

import cats.effect.IO


trait SpeakGenerator extends (Speaks#Speak => IO[Unit])
