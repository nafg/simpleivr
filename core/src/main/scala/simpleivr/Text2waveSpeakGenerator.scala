package simpleivr

import java.nio.channels.Channels
import java.nio.file.Files

import scala.io.Source

import cats.effect.IO


object Text2waveSpeakGenerator extends SpeakGenerator {
  override def apply(speak: Speaks#Speak) =
    speak.files.exists.flatMap {
      case true  => IO.unit
      case false =>
        speak.files.wavFile
          .write { writeChan =>
            IO {
              println("Falling back to text2wave because audio file does not exist: " + speak.files.supportedAudioFiles)
              val tmpFile = Files.createTempFile("chavrusa-text2wave", ".wav")
              val text2wave = Runtime.getRuntime.exec("/usr/bin/text2wave -scale 1.5 -F 8000 -o " + tmpFile.toString)
              val stdin = text2wave.getOutputStream
              stdin.write(speak.msg.getBytes())
              stdin.flush()
              stdin.close()
              text2wave.waitFor()
              Source.fromInputStream(text2wave.getErrorStream).getLines() foreach println
              Files.copy(tmpFile, Channels.newOutputStream(writeChan))
            }
          }
          .flatMap { either =>
            IO {
              either.left.foreach(_.printStackTrace())
            }
          }
    }
}
