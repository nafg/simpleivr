package simpleivr

import java.io.File


case class AudioPath(parent: String, name: String) {
  lazy val supportedAudioFiles =
    Seq("wav", "sln", "ulaw")
      .map(ext => ext -> new File(parent, name + "." + ext))
      .toMap

  lazy val wavFile = supportedAudioFiles("wav")
  lazy val slnFile = supportedAudioFiles("sln")

  def pathAndName: String = parent + "/" + name

  def existingFiles() = supportedAudioFiles.filter(_._2.exists())

  def exists() = existingFiles().nonEmpty
}

object AudioPath {
  private val removeExtensionRegex = """\.[^./\\]*$""".r
  def fromFile(file: File) =
    AudioPath(file.getParentFile.getAbsolutePath, removeExtensionRegex.replaceAllIn(file.getName, ""))
}
