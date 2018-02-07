package simpleivr

import scala.io.Source

import cats.effect.IO


object Text2waveSpeakGenerator extends SpeakGenerator {
  override def apply(speak: Sayables#Speak) = IO {
    if (!speak.files.exists()) {
      println("Falling back to text2wave because audio file does not exist: " + speak.files.supportedAudioFiles)
      val file = speak.files.wavFile
      val text2wave = Runtime.getRuntime.exec("/usr/bin/text2wave -scale 1.5 -F 8000 -o " + file.getAbsolutePath)
      val os = text2wave.getOutputStream
      os.write(speak.msg.getBytes())
      os.flush()
      os.close()
      text2wave.waitFor()
      Source.fromInputStream(text2wave.getInputStream).getLines() foreach println
      Source.fromInputStream(text2wave.getErrorStream).getLines() foreach println
      file.setWritable(true, false)
    }
  }
}
