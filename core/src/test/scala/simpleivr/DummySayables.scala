package simpleivr

import java.nio.file.Files


object DummySayables extends Sayables({
  val dir = Files.createTempDirectory("simpleivr_DummySayables")
  new LocalAudioFileBackend(dir, dir.toString)
})
