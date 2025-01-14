package rtj.part5polymorphic

import cats.effect.kernel.Deferred
import cats.effect.Concurrent
import cats.effect.Ref

import scala.collection.immutable.Queue
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.syntax.monadCancel._

/** generic version of mutex implemented in previous chapter */
abstract class Polymorphic3CoordinationMutex[F[_]] {
  def acquire: F[Unit]
  def release: F[Unit]
}

object Polymorphic3CoordinationMutex {
  type Signal[F[_]] = Deferred[F, Unit]
  case class State[F[_]](locked: Boolean, waiting: Queue[Signal[F]])

  def unlocked[F[_]]: State[F] = State[F](locked = false, Queue())
  def createSignal[F[_]](implicit concurrent: Concurrent[F]): F[Signal[F]] = concurrent.deferred[Unit]

  def create[F[_]](implicit concurrent: Concurrent[F]): F[Polymorphic3CoordinationMutex[F]] =
    concurrent
      .ref(unlocked)
      .map { initialState =>
        // dirty thing to make it compiling with Scala2
        val stateCoercedType = initialState.asInstanceOf[Ref[F, State[F]]]
        createMutexWithCancellation(stateCoercedType)
      }

  def createMutexWithCancellation[F[_]](
      state: Ref[F, State[F]],
    )(
      implicit concurrent: Concurrent[F],
    ): Polymorphic3CoordinationMutex[F] =
    new Polymorphic3CoordinationMutex[F] {
      override def acquire: F[Unit] = concurrent.uncancelable { poll =>
        createSignal.flatMap { signal =>
          val cleanup = state.modify { case State(locked, queue) =>
            val newQueue = queue.filterNot(_ eq signal)
            State(locked, newQueue) -> release
          }.flatten

          state.modify {
            case State(false, _) => State[F](locked = true, Queue()) -> concurrent.unit
            case State(true, queue) =>
              State[F](locked = true, queue.enqueue(signal)) -> poll(signal.get).onCancel(cleanup)
          }.flatten
        }
      }

      override def release: F[Unit] = state.modify {
        case State(false, _) => unlocked[F] -> concurrent.unit
        case State(true, queue) =>
          if (queue.isEmpty) unlocked[F] -> concurrent.unit
          else {
            val (signal, rest) = queue.dequeue
            State[F](locked = true, rest) -> signal.complete(()).void
          }
      }.flatten
    }

}
