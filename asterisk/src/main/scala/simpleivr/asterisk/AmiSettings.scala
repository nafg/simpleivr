package simpleivr.asterisk

trait AmiSettings {
  def peer: String
  def asteriskHost: String
  def agiHost: String
  def callerIdNum: String
  def callerIdName: String
  def amiUsername: String
  def amiPassword: String
}
