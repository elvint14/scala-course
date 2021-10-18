package tl

object TypeLevel2A {

  import scala.reflect.runtime.universe._
  def show[A](a: A)(implicit tag: TypeTag[A]) = tag.toString().replace("tl.TypeLevel2.", "")

  trait Nat
  class _0 extends Nat
  class Succ[N <: Nat] extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[_1] // Succ[Succ[_0]]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]

  // _2 < _4

  trait <[A <: Nat, B <: Nat]
  object < {
    implicit def ltBasic[B <: Nat]: <[_0, Succ[B]] =
      new <[_0, Succ[B]] {}
    implicit def ltInductive[A <: Nat, B <: Nat](implicit lt: <[A, B]): <[Succ[A], Succ[B]] =
      new <[Succ[A], Succ[B]] {}

    def apply[A <: Nat, B <: Nat](implicit lt: <[A, B]) = lt
  }

  trait <=[A <: Nat, B <: Nat]
  object <= {
    implicit def lteBasic[B <: Nat]: <=[_0, B] =
      new <=[_0, B] {}
    implicit def lteInductive[A <: Nat, B <: Nat](implicit lte: <=[A, B]): Succ[A] <= Succ[B] =
      new <=[Succ[A], Succ[B]] {}

    def apply[A <: Nat, B <: Nat](implicit lte: <=[A, B]) = lte
  }

  trait +[A <: Nat, B <: Nat, S <: Nat]
  object + {
    implicit val zero: +[_0, _0, _0] = new +[_0, _0, _0] {}
    // A + 0 = A
    implicit def basicRight[A <: Nat](implicit lt: _0 < A): +[_0, A, A] =
      new +[_0, A, A] {}
    // 0 + A = A
    implicit def basicLeft[A <: Nat](implicit lt: _0 < A): +[A, _0, A] =
      new +[A, _0, A] {}
    // A + B = S ===> Succ[A] + Succ[B] = Succ[Succ[S]]
    implicit def inductive[A <: Nat, B <: Nat, S <: Nat](
        implicit plus: +[A, B, S],
      ): +[Succ[A], Succ[B], Succ[Succ[S]]] =
      new +[Succ[A], Succ[B], Succ[Succ[S]]] {}

    def apply[A <: Nat, B <: Nat, S <: Nat](implicit plus: +[A, B, S]): +[A, B, S] = plus
  }

  val zero: +[_0, _0, _0] = +[_0, _0, _0] // compiles well !!!
  val twoL: +[_0, _2, _2] = +.apply
  val twoR: +[_3, _0, _3] = +.apply
  val four: +[_3, _1, _4] = +.apply
  val five: +[_3, _2, _5] = +.apply

  val comparison1: _0 < _1 = <[_0, _1]
  val comparison2: _1 < _3 = <[_1, _3]
//  val comparison3: _3 < _2 = <[_3, _2]
  val comparison4: _1 < _4 = <[_1, _4]

  val comparison5: _1 <= _1 = <=[_1, _1]
//  val comparison6: _2 <= _1 = <=[_2, _1]

  def main(xs: Array[String]): Unit = {
    println(show(comparison4))
    println(show(zero))
    println(show(twoL))
    println(show(twoR))
  }

}
