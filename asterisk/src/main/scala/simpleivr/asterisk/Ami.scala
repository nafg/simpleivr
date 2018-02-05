package simpleivr.asterisk

import java.beans.PropertyChangeEvent
import java.time.Instant

import org.asteriskjava.live._


class Ami(settings: AmiSettings)
  extends DefaultAsteriskServer(settings.asteriskHost, settings.amiUsername, settings.amiPassword) {

  def originate(dest: String, script: String, args: Seq[String]): Unit = try {
    val scriptAndArgs = script +: args
    var done = false
    val startTime = System.currentTimeMillis()
    println(s"Executing call for $scriptAndArgs at ${Instant.now}")
    var chan: Option[AsteriskChannel] = None
    import scala.collection.JavaConverters._
    originateToApplicationAsync(
      s"SIP/${settings.peer}/1$dest",
      "Agi",
      s"agi://${settings.agiHost}/${scriptAndArgs.mkString(",")}",
      60000,
      new CallerId(settings.callerIdName, settings.callerIdNum),
      Map("DEST_NUM" -> dest).asJava,
      new OriginateCallback {
        override def onDialing(channel: AsteriskChannel): Unit = {
          println("Dialing " + dest)
        }
        override def onSuccess(channel: AsteriskChannel): Unit = {
          println("Success! " + scriptAndArgs)
          chan = Some(channel)
          channel.addPropertyChangeListener(
            "state",
            (_: PropertyChangeEvent) => if (channel.getState == ChannelState.HUNGUP) done = true
          )
        }
        override def onNoAnswer(channel: AsteriskChannel): Unit = {
          println("No answer " + scriptAndArgs)
          done = true
        }
        override def onBusy(channel: AsteriskChannel): Unit = {
          println("Busy " + scriptAndArgs)
          done = true
        }
        override def onFailure(cause: LiveException): Unit = {
          println("Failure for " + scriptAndArgs + ": " + cause)
          done = true
        }
      }
    )

    while (System.currentTimeMillis() - startTime < 120000 && !done) Thread.sleep(10000)

    println("Finished executing call for " + scriptAndArgs + " at " + System.currentTimeMillis() + ", done = " + done + ", channel = " + chan)
  } catch {
    case e: Exception => e.printStackTrace()
  }
}
