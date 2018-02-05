package simpleivr

import java.io.File


case class AudioPath(directory: File, name: String) {
  lazy val supportedAudioFiles =
    Seq("wav", "sln", "ulaw")
      .map(ext => ext -> new File(directory, name + "." + ext))
      .toMap

  lazy val wavFile = supportedAudioFiles("wav")
  lazy val slnFile = supportedAudioFiles("sln")

  def pathAndName: String = directory.getAbsolutePath + File.separator + name

  def existingFiles() = supportedAudioFiles.filter(_._2.exists())

  def exists() = existingFiles().nonEmpty
}

object AudioPath {
  private val removeExtensionRegex = """\.[^./\\]*$""".r
  def fromFile(file: File) =
    AudioPath(file.getParentFile, removeExtensionRegex.replaceAllIn(file.getName, ""))
}
