package simpleivr

import java.io.File


case class AudioBase(localDir: File, path: String)
object AudioBase {
  def apply(dir: File): AudioBase = AudioBase(dir, dir.getAbsolutePath)
}
