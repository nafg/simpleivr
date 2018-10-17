package simpleivr

import java.nio.channels.{FileChannel, WritableByteChannel}
import java.nio.file.{Files, Path, StandardOpenOption}

import cats.effect.IO


case class LocalAudioFile(path: Path) extends AudioFile {
  override def name = path.toString

  override def exists = IO {
    Files.exists(path)
  }

  override def write(f: WritableByteChannel => IO[Unit]): IO[Either[Throwable, Unit]] =
    for {
      fileChan <- IO {
        Files.createDirectories(path.getParent)
        FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
      }
      res <- f(fileChan).attempt
      _ <- IO {
        fileChan.close()
      }
    } yield res

  override def lastModified = IO {
    Files.getLastModifiedTime(path).toInstant
  }
}

case class LocalAudioFiles(parent: Path, name: String) extends AudioFiles {
  override type FileType = LocalAudioFile

  override lazy val supportedAudioFiles =
    Seq("wav", "sln", "ulaw")
      .map(ext => ext -> LocalAudioFile(parent.resolve(s"$name.$ext")))
      .toMap
}

class LocalAudioFileBackend(val localDir: Path, val path: String) extends AudioFileBackend {
  override type FilesType = LocalAudioFiles

  override def speakFiles(speak: Speaks#Speak) = LocalAudioFiles(localDir, speakFilename(speak))
  override def speakPath(speak: Speaks#Speak) = IO.pure(AudioPath(path + "/" + speakFilename(speak)))

  def toAudioPathPure(localAudioFiles: LocalAudioFiles) =
    AudioPath(path + "/" + localDir.relativize(localAudioFiles.parent) + "/" + localAudioFiles.name)

  override def toAudioPath(localAudioFiles: LocalAudioFiles) = IO.pure(toAudioPathPure(localAudioFiles))
}
