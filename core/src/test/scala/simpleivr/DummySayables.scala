package simpleivr

import java.nio.file.Files


class DummySayables extends Sayables({
  val dir = Files.createTempDirectory("simpleivr_DummySayables")
  new LocalAudioFileBackend(dir, dir.toString)
})

object DummySayables extends DummySayables
