package simpleivr

abstract class SingleAudioFile(format: String) extends AudioFiles with AudioFile {
  override type FileType = this.type
  override def supportedAudioFiles = Map(format -> this)
}
