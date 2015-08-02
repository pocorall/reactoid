package org.reactoid.sample

import android.view.View
import org.reactoid.all._
import org.scaloid.common._
import rx.core.Obs
import rx.{Rx, Var}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions


/**
 * Provides utilities simulate asynchronous IO. The actual IO can be TCP, disk operation or anything you name.
 */
object AsyncIOSimulator {
  private var i = 0

  /** Simulates server connection. */
  def isValidID(str: CharSequence): Future[Boolean] = whenDirty {
    i += 1
    val v = i % 3
    Thread.sleep(2000) // simulates latency
    if (v == 0) throw new Exception("Connection Timeout")
    v == 1
  }

  def validityMessage(str: CharSequence): Future[String] =
    isValidID(str).map {
      valid => if (valid) "Valid ID" else "ID already exists"
    }.recover { case cause => cause.getMessage }

  def update[T](job: => Unit, delay: Int = 4000): Unit = {
    Future {
      while (true) {
        Thread.sleep(delay)
        job
      }
    }
  }

  var o: Obs = null
}

import org.reactoid.sample.AsyncIOSimulator._

class HelloReactoid extends SActivity {
  onCreate {
    contentView = new SVerticalLayout {
      val lyout = this

      def demo1() = {
        val txt = EditText("Hello").textVar
        Button(txt() + ", world!")
      }

      def demo2() = {
        val tv = TextView()
        val txt = EditText().hint("ID").textVar
        val msgRx = validityMessage(txt()): Rx[String]
        tv.observe(msgRx)
      }

      def demo3() = {
        case class Item(title: Var[String], unitPrice: Var[Double], quantity: Var[Int]) {
          val price = Rx(quantity() * unitPrice())
        }

        trait ViewBuilder[T] {
          def createView: View
        }

        implicit class ItemViewBuilder(i: Item) extends ViewBuilder[Item] {
          def createView: View = {
            new SVerticalLayout {
              TextView(i.title)
              EditText(i.unitPrice)
              EditText(i.quantity)
              TextView(i.price)
            }
          }
        }

        import scala.language.higherKinds

        implicit class SeqViewBuilder[T, S[Q] <: Seq[Q]](seq: S[T])(implicit ev: T => ViewBuilder[T]) extends ViewBuilder[S[T]] {
          def createView: View = {
            new SVerticalLayout {
              seq.foreach(i => this += i.createView)
            }
          }
        }

        implicit class ListViewBuilder[T, S[Q] <: List[Q]](list: S[T])(implicit ev: T => ViewBuilder[T]) extends ViewBuilder[S[T]] {
          def createView: View = {
            new SVerticalLayout {
              list.foreach {
                i =>
                  this += i.createView
                  TextView("Remove")
              }
            }
          }
        }

        implicit class RxViewBuilder[T, S[Q] <: Rx[Q]](rx: S[T])(implicit ev: T => ViewBuilder[T]) extends ViewBuilder[S[T]] {
          def createView: View = new SVerticalLayout {
            this += rx().createView
            TextView("RXVIEW")
          }
        }

        implicit class EditableViewBuilder[T](rxList: Var[List[T]])(implicit ev: T => ViewBuilder[T]) extends ViewBuilder[Var[List[T]]] {
          def createView: View = {
            new SVerticalLayout {
              val l = rxList()
              l.indices.foreach {
                i =>
                  this += l(i).createView
                  this += new
                      SLinearLayout {
                    if (i > 0) SButton("↑", rxList() = l.updated(i, l(i - 1)).updated(i - 1, l(i))).wrap
                    if (i < l.length - 1) SButton("↓", rxList() = l.updated(i, l(i + 1)).updated(i + 1, l(i))).wrap
                    SButton("-", rxList() = l.patch(i, Nil, 1)).wrap
                  }
              }
              SButton("+", toast("ADD"))
            }
          }
        }

        val cart = Var(List(
          Item("Tomato", 0.5, 1),
          Item("Apple", 0.99, 3),
          Item("Pear", 1.05, 3),
          Item("Strawberry", 3.0, 1)
        ))

        update {
          val u = cart()(2).unitPrice
          u() = u() * 0.95
        }

        def build[T](obj: Var[T])(implicit ev: Var[T] => ViewBuilder[Var[T]]) = {
          o = Obs(obj) {
            lyout.removeAllViews()
            lyout += obj.createView
          }
        }

        build(cart)
      }

      demo3()
    } padding 20.dip
  }
}
