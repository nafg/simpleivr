package simpleivr

import java.nio.channels.WritableByteChannel
import java.time.Instant

import cats.effect.IO
import cats.implicits._


trait AudioFile {
  def name: String
  def exists: IO[Boolean]
  def write(f: WritableByteChannel => IO[Unit]): IO[Either[Throwable, Unit]]
  def lastModified: IO[Instant]
}

trait AudioFiles {
  def name: String

  type FileType <: AudioFile

  def supportedAudioFiles: Map[String, FileType]

  lazy val wavFile = supportedAudioFiles("wav")
  lazy val slnFile = supportedAudioFiles("sln")
  lazy val ulawFile = supportedAudioFiles("ulaw")

  def existingFiles =
    supportedAudioFiles.toList.traverse(t => t._2.exists.map(_ -> t)).map(_.collect { case (true, t) => t }.toMap)

  def exists: IO[Boolean] = existingFiles.map(_.nonEmpty)
}

trait AudioFileBackend {
  type FilesType <: AudioFiles

  def slugify(text: String) = text.replaceAll("\\W", "-").trim.toLowerCase
  def speakFilename(speak: Speaks#Speak): String = slugify(speak.msg)

  def speakFiles(speak: Speaks#Speak): FilesType
  def toAudioPath(audioFiles: FilesType): IO[AudioPath]
  def speakPath(speak: Speaks#Speak): IO[AudioPath] = toAudioPath(speakFiles(speak))
}
