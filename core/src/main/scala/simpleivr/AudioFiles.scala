package simpleivr

import java.io.File


/**
  * Represents a collection of audio files with the same name
  * and different formats
  *
  * @param parent the base directory
  * @param name   the filename without the extension
  */
case class AudioFiles(parent: File, name: String) {
  lazy val supportedAudioFiles =
    Seq("wav", "sln", "ulaw")
      .map(ext => ext -> new File(parent, name + "." + ext))
      .toMap

  lazy val wavFile = supportedAudioFiles("wav")
  lazy val slnFile = supportedAudioFiles("sln")

  def existingFiles() = supportedAudioFiles.filter(_._2.exists())

  def exists() = existingFiles().nonEmpty
}
