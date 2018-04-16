package simpleivr

import java.io.File


object DummySayables extends Sayables(AudioBase(File.createTempFile("simpleivr_DummySayables", "")))
