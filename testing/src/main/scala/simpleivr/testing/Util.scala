package simpleivr.testing

object Util {
  def spans[A](xs: List[A])(p: A => Boolean): List[List[A]] = {
    def loop(xs: List[A])(cur: List[A]): List[List[A]] =
      xs match {
        case Nil      => cur.reverse :: Nil
        case hd :: tl =>
          if (!p(hd))
            loop(tl)(hd :: cur)
          else {
            val remaining = loop(tl)(Nil)
            if (cur.isEmpty)
              remaining
            else
              cur.reverse :: remaining
          }
      }

    loop(xs)(Nil)
  }
}
