package simpleivr.testing

import org.scalactic.source
import org.scalatest.Assertions


trait InteractionTest extends InteractionTestBase with Assertions {
  implicit def runWithConfig(implicit pos: source.Position): RunWithConfig = RunWithConfig(msg => fail(msg))
}
