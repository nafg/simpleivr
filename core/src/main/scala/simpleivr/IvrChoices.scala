package simpleivr

import cats.free.Free
import cats.implicits._


class IvrChoices(sayables: Sayables) extends Ivr(sayables) {

  import sayables._


  case class Choice[+A](key: Option[Char], label: Sayable, action: A) {
    def map[B](f: A => B): Choice[B] = Choice(key, label, f(action))
  }
  object Choice {
    def apply[A](label: Sayable, action: A): Choice[A] = new Choice(None, label, action)
    def apply[A](key: Char, label: Sayable, action: A): Choice[A] = new Choice(Some(key), label, action)
  }

  def paginated[T](maximum: Int, choices: List[Choice[T]], fixedChoices: List[Choice[T]]): List[Choice[IvrStep[T]]] = {
    def doPage(page: List[Choice[T]], rest: List[Choice[T]], back: List[Choice[T]]): List[Choice[IvrStep[T]]] = {
      def askNextPage: IvrStep[T] = Free.defer {
        val (p, r) = rest.splitAt(7)
        val b = page ::: back
        askChoice(ChoiceMenu(SayNothing, doPage(p, r, b))).flatten
      }

      def askPrevPage: IvrStep[T] = Free.defer {
        if (back.length >= maximum) {
          val num = if (back.length <= 8) back.length else 7
          val (page2, back2) = back.splitAt(num)
          val rest2 = page ::: rest
          askChoice(ChoiceMenu(SayNothing, doPage(page2, rest2, back2))).flatten
        } else {
          val (page2, rest2) = choices.splitAt(maximum - 1)
          val back2 = Nil
          askChoice(ChoiceMenu(SayNothing, doPage(page2, rest2, back2))).flatten
        }
      }

      page.map(_ map IvrStep.apply) ++
        List(Choice('8', `For more choices`, askNextPage)).filter(_ => rest.nonEmpty) ++
        List(Choice('9', `For the previous choices`, askPrevPage)).filter(_ => back.nonEmpty) ++
        fixedChoices.map(_ map IvrStep.apply)
    }

    doPage(choices.take(maximum - 1), choices.drop(maximum - 1), Nil)
  }

  def assignNums[A](choices: List[Choice[A]]): List[Choice[A]] = {
    val unused = "1234567890".toList.filterNot(n => choices exists (_.key contains n))
    println("assignNums: unused: " + unused.mkString)
    for (c <- choices) println(s"  ${c.key} / ${c.label}")
    assignNums[A](choices, unused)
  }
  def assignNums[A](choices: List[Choice[A]], nums: List[Char]): List[Choice[A]] = choices match {
    case Nil                                 => Nil
    case (c @ Choice(Some(n), _, _)) :: rest => c :: assignNums(rest, nums.filter(_ != n))
    case c :: rest                           =>
      if (nums.nonEmpty)
        c.copy(key = Some(nums.head)) :: assignNums(rest, nums.tail)
      else {
        println("ERROR: No num available for choices: " + choices)
        c :: rest
      }
  }

  case class ChoiceMenu[A](title: Sayable, choices: Seq[Choice[A]])
  object ChoiceMenu {
    def apply[A](title: Sayable, dummy: Null = null)(choices: Choice[A]*): ChoiceMenu[A] =
      new ChoiceMenu(title, choices)
  }

  def askChoice[A](choiceMenu: ChoiceMenu[A]): IvrStep[A] = {
    val menu = assignNums(choiceMenu.choices.toList)
    val word: Char => Sayable = {
      case '*' => `star`
      case '#' => `pound`
      case c   => digitWords(c.toString)
    }
    val menuMsgs = menu.collect {
      case Choice(Some(key), label, _) => Pause(750) & `Press` & word(key) & label
    }
    if (menuMsgs.length < menu.length)
      Console.err.println(s"ERROR: Not all menu choices have keys in ${choiceMenu.title}: $menu")

    def loop: IvrStep[A] = sayAndGetDigit(choiceMenu.title & menuMsgs) flatMap {
      case None    => IvrStep.say(`Please make a selection` & Pause(750)) *> loop
      case Some(c) =>
        menu.find(_.key.contains(c)) match {
          case Some(choice) => IvrStep(choice.action)
          case None         => IvrStep.say(`That is not one of the choices.` & Pause(750)) *> loop
        }
    }

    loop
  }
}
