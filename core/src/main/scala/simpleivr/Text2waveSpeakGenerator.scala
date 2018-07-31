package simpleivr

import java.nio.ByteBuffer
import java.nio.channels.Channels

import scala.io.Source

import cats.effect.IO


object Text2waveSpeakGenerator extends SpeakGenerator {
  override def apply(speak: Sayables#Speak) =
    speak.files.exists.flatMap {
      case true  => IO.unit
      case false =>
        speak.files.wavFile
          .write { writeChan =>
            IO {
              println("Falling back to text2wave because audio file does not exist: " + speak.files.supportedAudioFiles)
              val text2wave = Runtime.getRuntime.exec("/usr/bin/text2wave -scale 1.5 -F 8000")
              val stdin = text2wave.getOutputStream
              stdin.write(speak.msg.getBytes())
              stdin.flush()
              val stdout = Channels.newChannel(text2wave.getInputStream)
              val buf = ByteBuffer.allocate(8192)
              Iterator.continually {
                buf.clear()
                stdout.read(buf)
              }
                .takeWhile(_ >= 0)
                .foreach { _ =>
                  buf.flip()
                  writeChan.write(buf)
                }
              text2wave.getInputStream
              stdin.close()
              stdout.close()
              text2wave.waitFor()
              Source.fromInputStream(text2wave.getErrorStream).getLines() foreach println
            }
          }
          .flatMap { either =>
            IO {
              either.left.foreach(_.printStackTrace())
            }
          }
    }
}
