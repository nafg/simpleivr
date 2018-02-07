package simpleivr

import java.io.File


case class AudioPath(pathAndName: String)

object AudioPath {
  def apply(path: String, name: String): AudioPath = AudioPath(path + "/" + name)

  private val removeExtensionRegex = """\.[^./\\]*$""".r

  def fromFile(file: File) =
    AudioPath(file.getParentFile.getAbsolutePath, removeExtensionRegex.replaceAllIn(file.getName, ""))

  def fromAudioFiles(audioFiles: AudioFiles) =
    AudioPath(audioFiles.parent.getAbsolutePath, audioFiles.name)
}
