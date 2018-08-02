package simpleivr

object DTMF extends Enumeration {
  val `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `0` = Value
  val `#` = Value("#")
  val * = Value("*")

  val fromChar: Map[Char, DTMF.Value] =
    "0123456789#*".map(c => c -> withName(c.toString)).toMap

  val toChar: Map[DTMF.Value, Char] = fromChar.map(_.swap)

  implicit class DTMF_ExtensionMethods(private val self: DTMF.Value) extends AnyVal {
    def toChar = DTMF.toChar(self)
    def isDigit = toChar.isDigit
  }

  lazy val digits = values.filter(_.isDigit)
}
