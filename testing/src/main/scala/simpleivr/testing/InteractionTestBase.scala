package simpleivr.testing

import cats.{Id, catsInstancesForId}
import simpleivr.{IvrCommand, IvrStep}

import scala.io.AnsiColor


trait InteractionTestBase {
  case class RunWithConfig(error: String => Nothing, logIvrCommand: (IvrCommand[_], Any) => Unit = (_, _) => ()) {
    def withDefaultLogIvrCommand: RunWithConfig =
      copy(
        logIvrCommand =
          (ivrCommand, result) => println(s"${AnsiColor.YELLOW}   $ivrCommand  -->  $result${AnsiColor.RESET}")
      )
  }

  implicit class IvrStepRunWith[A](self: IvrStep[A]) {
    def runWith(callerId: String = null)(interactions: Interactions)(implicit config: RunWithConfig): A = {
      lazy val interp: InteractionIvrCommandInterpreter =
        new InteractionIvrCommandInterpreter(interactions.interactions.reverse, callerId)({ ignoredCount =>
          if (ignoredCount >= 100)
            config.error(
              s"The remaining ${interp.interactions.length} interactions have been ignored $ignoredCount times, " +
                s"possibly indicating an infinite loop. These interactions remain: ${interp.interactions.mkString(", ")}"
            )
        })
      val ret =
        self.runM[Id] { cmdF =>
          val intermediate = cmdF.ivrCommand.fold(interp)
          config.logIvrCommand(cmdF.ivrCommand, intermediate)
          cmdF.apply(intermediate)
        }
      if (interp.interactions.nonEmpty)
        config.error(s"Execution has completed yet these interactions remain: ${interp.interactions.mkString(", ")}")
      ret
    }

    def runOnly(callerId: String = null)(interactions: Interactions)(implicit config: RunWithConfig): Unit =
      try runWith(callerId)(interactions.hangUp)
      catch {
        case _: Interaction.HangUpException =>
      }
  }
}
