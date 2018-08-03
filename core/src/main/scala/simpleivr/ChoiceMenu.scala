package simpleivr

case class ChoiceMenu[A](title: Sayable, choices: Seq[Choice[A]]) {
  private def assignNums(choices: List[Choice[A]], nums: List[DTMF]): List[Choice.Assigned[A]] = choices match {
    case Nil                                       => Nil
    case (c @ Choice.Assigned(dtmf, _, _)) :: rest => c :: assignNums(rest, nums.filter(_ != dtmf))
    case Choice.Unassigned(label, value) :: rest   =>
      if (nums.nonEmpty)
        Choice.Assigned(nums.head, label, value) :: assignNums(rest, nums.tail)
      else {
        println("ERROR: No num available for choices: " + choices)
        assignNums(rest, nums)
      }
  }

  lazy val assigned =
    assignNums(choices.toList, DTMF.digits.toList.filterNot(n => choices exists (_.maybeDtmf contains n)))
}

object ChoiceMenu {
  def apply[A](title: Sayable, dummy: Null = null)(choices: Choice[A]*): ChoiceMenu[A] =
    new ChoiceMenu(title, choices)
}
